

## Projektöversikt

Gym Hype Coach (svensk apptitel i strings.xml: "Gympepp") är en enkel Android-app som peppar användaren under träningspasset. Flödet är medvetet minimalt: tryck START när ett set börjar, få peppande ord uppspelade medan du tränar, tryck STOPP när du är klar. Två knappar och en timer.

Appen byggs i tre steg:

- Steg 1: Spelaren. Förbakade peppfraser (wav) spelas upp på timer. Ingen AI på enheten.
- Steg 2: Lokal LLM (Gemma) plus Piper genererar nya peppfraser i bakgrunden, så att det inte bara blir samma statiska fraser hela tiden.
- Steg 3: Personlig, progressiv coach. Reps och vikt loggas i Room, och coachen ger målbaserad pepp vid set-start utifrån din egen historik.

## Bärande arkitekturprincip

LLM:en ligger ALDRIG i den heta loopen. Under passet ska inget tungt beräknas. Fraser genereras och syntetiseras i förväg i bakgrunden, cachas som wav-filer, och vid rätt tillfälle spelas bara en färdig fil upp. Det ger noll latens på gymmet.

Konsekvens: steg 2 ändrar inte runtime-spelaren. Det enda som byts är källan till wav-filerna, från bundlade defaultfiler (steg 1) till genererade och cachade filer (steg 2). Bygg därför steg 1 så att uppspelningen läser via en liten abstraktion (HypeSource), så att samma spelarkod kan peka om till cachen senare utan att röras.

## Teknisk stack

- Språk: Kotlin
- IDE: Android Studio
- UI: Jetpack Compose
- Arkitektur: MVVM
- minSdk: 26, targetSdk: senaste stabila
- Målenhet: Samsung Galaxy A33 (Exynos 1280). Mellanklass utan kraftfull NPU, så håll allt lättviktigt.

Steg 2 specifikt:

- LLM: Gemma 2B (eller Qwen 1.5B), Q4-kvantiserad, via MediaPipe LLM Inference (Google AI Edge).
- TTS: Piper via sherpa-onnx, med en paketerad röst som redan innehåller tokens.txt och espeak-ng-data.
- Bakgrundsjobb: WorkManager.

## Konventioner

- Inga tankstreck någonstans, varken i kod, kommentarer, commit-meddelanden eller svenska UI-texter. Använd komma, punkt, kolon eller parentes. I svenskan är tankstrecket reserverat för talstreck.
- Kod, identifierare och kommentarer skrivs på engelska. Användartexter skrivs på svenska via strings.xml.
- Repo-namn på engelska i kebab-case (gym-hype-coach). Apptitel på svenska i strings.xml.
- Branch-namn på engelska i feat/-format, till exempel feat/player-timer och feat/llm-hype-generation.

## Git-arbetsflöde

- Claude Code skapar feature-branches och öppnar pull requests.
- Claude Code mergar ALDRIG. Peter är ensam om att merga.
- En logisk enhet per branch och PR. Håll diffarna små och granskningsbara.

## Projektstruktur (förslag)

```
app/src/main/java/dev/peterbot/gymhypecoach/
  ui/            # Compose-skärmar och komponenter
  viewmodel/     # ViewModels (MVVM)
  audio/         # Uppspelning och HypeSource
  llm/           # Steg 2: Gemma-generering (MediaPipe)
  tts/           # Steg 2: Piper-syntes (sherpa-onnx)
  work/          # Steg 2: WorkManager-jobb
  data/          # Steg 3: Room (entities, dao, repository)
  coaching/      # Steg 3: progressionslogik och mallhantering
app/src/main/res/raw/   # Steg 1: bundlade hype_01.wav, hype_02.wav, osv
```

## Steg 1: Spelaren (gör detta först)

Mål: en fungerande app med START/STOPP, uppräknande timer och uppspelning av förbakade peppfraser.

Att bygga:

- En Compose-skärm med en stor START/STOPP-knapp och en timer (mm:ss).
- En ViewModel som håller isRunning och elapsed.
- Uppspelning via MediaPlayer av wav-filer. SoundPool är ett alternativ för riktigt korta klipp, men MediaPlayer räcker för MVP:n.
- Logik: vid START spela en slumpad fras direkt, sedan en ny var 25:e sekund tills STOPP. Undvik att spela samma fras två gånger i rad.
- Lägg in 15 till 20 förbakade fraser i res/raw. Dessa genereras separat på desktop (gärna med Kokoro för naturligare röst, latens spelar ingen roll för bundlat ljud). Filnamn med små bokstäver utan mellanslag, till exempel hype_01.wav.

Viktigt för steg 2: lägg läsningen av fraser bakom ett litet interface, så att källan kan bytas från res/raw till en cachekatalog utan att spelaren ändras.

```kotlin
interface HypeSource {
    fun next(): AudioSource   // returnerar nästa fras att spela (undvik direkt upprepning)
}
```

Referensskiss för kärnan i ViewModel (källan abstraherad bakom HypeSource):

```kotlin
class HypeViewModel(
    app: Application,
    private val hypeSource: HypeSource
) : AndroidViewModel(app) {

    private val player = MediaPlayer()
    private var loopJob: Job? = null

    var isRunning by mutableStateOf(false); private set
    var elapsed by mutableStateOf(0); private set

    fun start() {
        if (isRunning) return
        isRunning = true; elapsed = 0
        playNext()                              // pepp direkt vid start
        loopJob = viewModelScope.launch {
            while (isActive) {
                delay(1000); elapsed++
                if (elapsed % 25 == 0) playNext()   // ny pepp var 25:e sekund
            }
        }
    }

    fun stop() {
        isRunning = false
        loopJob?.cancel()
        player.reset()
    }

    private fun playNext() {
        val source = hypeSource.next()
        player.reset()
        source.applyTo(player)                  // sätter datasource (res/raw i steg 1, fil i steg 2)
        player.prepare(); player.start()
    }

    override fun onCleared() = player.release()
}
```

Compose-skärmen kan vara så enkel som:

```kotlin
@Composable
fun CoachScreen(vm: HypeViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("%02d:%02d".format(vm.elapsed / 60, vm.elapsed % 60))
        Button(onClick = { if (vm.isRunning) vm.stop() else vm.start() }) {
            Text(if (vm.isRunning) "STOPP" else "START")
        }
    }
}
```

Klart när: du kan trycka START på gymmet, höra pepp direkt och med jämna mellanrum, och trycka STOPP.

## Steg 2: Lokal LLM (Gemma) plus Piper

Mål: telefonen genererar själv nya peppfraser i bakgrunden, så att banken hålls färsk och inte bara består av de bundlade fraserna.

Att bygga:

- En persona-väljare i UI (förval plus fritextfält), till exempel "drillsergeant", "stöttande coach", "sarkastisk PT".
- En CachedHypeSource som implementerar HypeSource och läser wav-filer från appens cachekatalog. Spelaren från steg 1 ska kunna använda den utan ändringar.
- Ett WorkManager-jobb med constraints (kräver laddning, gärna enhet i vila) som:
  1. Kör Gemma via MediaPipe LLM Inference med en prompt i stil med: "Generate 30 short gym motivation phrases in the style of {persona}. Max 8 words each. One per line. English only." Parsar raderna.
  2. Matar varje fras genom Piper (sherpa-onnx) och sparar resultatet som wav i cachekatalogen.
  3. Pekar om CachedHypeSource till den nya uppsättningen filer.
- Piper-röst: använd en sherpa-paketerad engelsk high-röst, till exempel vits-piper-en_US-ryan-high. Den innehåller redan tokens.txt och espeak-ng-data, vilket Piper kräver för fonemiseringen. Hämtar du de råa rhasspy-filerna måste tokens-formatet och espeak-ng-data ordnas manuellt.

Prestanda och förbehåll:

- Gemma 2B på en A33 är den tyngsta delen av hela appen. Kör ALDRIG genereringen under passet, bara i bakgrunden på laddning.
- Första genereringen tar en stund. Visa gärna en diskret status i UI, till exempel "uppdaterar peppbank".
- Allt körs lokalt, inget moln. Det är hela poängen med projektet och portföljberättelsen.

Klart när: du kan välja en persona, låta telefonen generera en ny omgång fraser i bakgrunden på laddning, och sedan höra dessa nya fraser på nästa pass utan någon fördröjning på gymmet.

## Steg 3: Personlig, progressiv coach

Mål: coachen blir personlig genom att logga dina set i Room och ge målbaserad pepp vid set-start utifrån din egen historik, till exempel "you did 12 reps last time, give me 14".

Arkitekturnot: progressionsfrasen kan inte förbakas, eftersom siffrorna beror på övning och historik. Den hanteras därför med mall plus data, inte av LLM i stunden. Room ger siffrorna, en mall ger meningen, och Piper syntetiserar vid set-start. Cirka en sekunds syntes är okej där, eftersom du ändå laddar och ställer in. LLM:en ligger fortfarande utanför heta loopen, dess enda jobb är att skapa varierade mallar i bakgrunden.

### Room-datamodell

```kotlin
@Entity
data class Exercise(
    @PrimaryKey val id: Long = 0,
    val name: String,            // "Bench Press", "Squat" ...
    val repRangeLow: Int = 8,
    val repRangeHigh: Int = 12,
    val weightStepKg: Float = 2.5f
)

@Entity
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long = System.currentTimeMillis()
)

@Entity
data class SetEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis()
)
```

DAO behöver minst två saker: hämta senaste SetEntry för en given övning (för att veta förra gångens reps och vikt), samt sätta in nya set.

### Progressionslogik (dubbelprogression)

Hårdkoda inte plus två reps i all evighet, då plateauar du och frasen blir orealistisk. Använd dubbelprogression, ett enkelt och beprövat schema:

- Om förra reps är under repRangeHigh: behåll vikten, mål = förra reps plus 1.
- Om förra reps når repRangeHigh: öka vikten med weightStepKg och sätt mål = repRangeLow.
- Valfritt: om du missat målet flera pass i rad, lägg in en deload (sänk vikten en bit).

Lägg detta bakom ett litet ProgressionStrategy-interface så att schemat kan bytas senare.

### Mallar och frasbyggnad

LLM:en (samma WorkManager-jobb som i steg 2) genererar varierade mallskal med platshållare, i bakgrunden:

- "Last time {exercise} was {last} reps, give me {target}."
- "You hit {last}, now push for {target}."
- Vid viktökning: "New weight today, {weight} kilos, aim for {target} reps."

Vid set-start: läs senaste set från Room, kör ProgressionStrategy, fyll i en slumpad mall och syntetisera med Piper. Alternativt försyntetisera frasen direkt när du väljer dagens övning, så är även den sekunden borta.

### UI-förändringar

Detta tar appen från två knappar till en lätt tracker, så håll inmatningen snabb och med stora ytor för svettiga fingrar:

- Välj dagens övning före START. Visa gärna förra gångens resultat, till exempel "förra: 12 reps på 40 kg".
- Logga reps och vikt efter STOPP.

Klart när: du väljer en övning, hör en målbaserad startfras byggd på din faktiska historik, kör ditt set, loggar resultatet, och ser progressionen återspeglas på nästa pass.

## Bygg och kör

- Installera på enhet via Android Studio eller: ./gradlew installDebug
- Bygg och verifiera steg 1 helt utan AI-delen igång först. Lägg inte till MediaPipe eller sherpa-onnx förrän spelaren känns bra på riktigt.

## Important
I want the code to be modular easy to maintain and search for errors . I also want you to not take chanses , if you dont know something you check it with me and we can together solve the issue.

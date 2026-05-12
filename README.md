# Projektuppgift-1-DevSevOps-2026
Projektuppgift: DevSevOps kontinuerlig utveckling och automatiserad testning


Översikt
Ni ska under kursern bygga en fullstack-webbapplikation med ett REST API och sätta upp en komplett CI/CD-pipeline i GitHub Actions. Applikationens tema och omfattning väljer ni själva, men den måste uppfylla de tekniska kraven nedan.

Applikationen ska lösa ett verkligt eller påhittat problem. En todo-app är godkänd, men en app med bara en resurs som heter “Item” med fältet “name” är inte tillräckligt. Er huvudresurs ska ha minst 3 fält/attribut utöver ID.

Om du arbetar själv och har en färdig fullstack app sedan tidigare - ok att återanvända, men skriv test- och devop-flöden under kursen!
Om ni arbetar tillsammans i grupp – diskutera / planera / välj ut en fullstack app att skapa/vidare utveckla – och skriv test- och devop-flöden under kursen!
Tekniska krav
Backend (valfritt språk: Node.js, Python, C#, Java, Go, etc.)
REST API med minst 2 endpoints (per deltagare) som implementerar:
GET – hämta en eller flera resurser
POST – skapa ny resurs
PUT eller PATCH – uppdatera resurs
DELETE – ta bort resurs
Någon form av datalagring (databas, JSON-fil, eller in-memory med seed-data)
Korrekt användning av HTTP-statuskoder (200, 201, 400, 404, etc.)
Tips! Om du inte vill använda en “riktig databas”
JSON-server är en npm-modul (npm install json-server) som tillåter GET/POST/PUT/DELETE requests mot en JSON-fil och automatiskt ger dig ett REST-api. Dokumentation här. Vi går igenom detta bibliotek vecka 6!

Frontend (vanilla JS/TS eller valfritt ramverk: React, Vue, Svelte, Angular)
Ska konsumera API:et och visa data för användaren
Användaren ska kunna utföra minst 3 av 4 CRUD-operationer via gränssnittet
Grundläggande felhantering (visa meddelande om något går fel)
Tester
Vitest/Jest eller annat adekvat bibliotek – enhetstester för backend-logik
Playwright/Cypress eller annat adekvat bibliotek – end-to-end-tester för frontend
Postman/Newman eller annat adekvat bibliotek – API-tester
GitHub Actions Pipeline
Workflow som triggas på push och/eller pull_request
Ska köra alla tester
Ska passera (grön bock) vid inlämning
Individuell rapport
Du ska skriva och lämna in en rapport där du beskriver vad applikationen gör, vilken testning du implementerat, hur väl testningen täcker applikationens olika delar, och hur bra testbarheten i applikationen är (Kan allt testas? Om inte, varför ? Vad skulle behöva göras för att uppnå full testbarhet, om möjligt).
Beskriv vidare vad som är implementerat i pipeline; steg, gates, logik och funktionalitet.
Beskriv svårigheter och eventuella luckor/problem i pipeline.
Avsluta med en reflektion om projektet, kursen, vad du lärt dig, och vad du skulle vilja lära dig mer inom området.
Rapporten bedöms sammantaget med inlämningen jämte redovisning och observation i förhållande till kursens betygskriterier.

Betygskriterier
Betygskriterierna är baserade på kursplanens lärandemål och bedömningsgrunder och är till för att skapa ett bra underlag för bedömning enligt dessa.

Godkänt (G)
Kriterium	Skallkrav
API	2 endpoints per deltagare (GET, POST, PUT/PATCH, DELETE) som fungerar
Frontend	Visar data från API, minst 3 av 4 CRUD-operationer fungerar
Tester	Minst 3 tester per testtyp och deltagare, som passerar och testar reell funktionalitet
Pipeline	GitHub Actions workflow som kör tester och passerar
Branch protection	Main-branch är skyddad – kräver att workflow passerar före merge
Dokumentation	README med: hur man startar projektet, kort beskrivning av applikationen
Kod	Går att klona och köra lokalt med dokumenterade instruktioner
Väl Godkänt (VG)
Uppfyller alla krav för G, plus minst 4 av följande:

Kriterium	Skallkrav
Testbredd	Minst 6 tester per testtyp och deltagare
Kodkvalitet	Automatisk kodstilskontroll (t.ex. ESLint, Pylint) som körs i pipeline
Säkerhet	Pipeline kontrollerar att inga dependencies har kända säkerhetsvarningar (t.ex. npm audit, pip-audit)
Produktionsbygge	Om ramverket har produktionsläge: testerna körs mot produktionsversionen, inte utvecklingsversionen
API-design	Validering av input (t.ex. “namn får inte vara tomt”), tydliga felmeddelanden till användaren
Frontend UX	Minst 2 av: Fungerar på dator och mobil, bekräftelse innan radering, felmeddelanden visas för användaren
Branch protection	Kräver godkänd code review från annan person utöver att tester passerar
Dokumentation	Instruktioner för hur API:et används (vilka endpoints finns, vad skickar man in, vad får man tillbaka)
Inlämning
Checklista
Repo är publikt eller lärare är inbjuden som collaborator
README innehåller startinstruktioner
GitHub Actions workflow syns och passerar
Alla endpoints kan testas (bifoga Postman-collection eller curl-exempel)
Redovisning
Var beredd att:

Förklara era arkitekturbeslut
Visa att du förstår er kodbas
Svara på frågor om pipeline-konfigurationen
Exempelapplikationer (inspiration)
För att ge er en känsla för lämplig omfattning:

Bokhantering för ett bibliotek
Receptsamling med ingredienser
Enkelt bokningssystem
Produktkatalog med kategorier
Quiz-applikation med frågor och resultat
Notes-app
Avancerad Todo-app

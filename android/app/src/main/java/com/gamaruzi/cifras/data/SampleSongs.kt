package com.gamaruzi.cifras.data

// Cifras-demo do handoff (docs/design/songs.jsx). Autoria original fictícia —
// substituir pela leitura real de arquivos quando o SAF picker entrar (Scope B).
object SampleSongs {
    val ALL: List<Song> = listOf(
        Song(
            id = "manha-estrada",
            file = "Manhã na Estrada - Rio Vermelho.txt",
            title = "Manhã na Estrada",
            artist = "Rio Vermelho",
            key = "G",
            capo = 0,
            genre = "Folk",
            ext = "txt",
            sections = listOf(
                Section("Intro", listOf(
                    Line("G   D   Em  C", "")
                )),
                Section("Verso", listOf(
                    Line("G             D", "A manhã chega devagar"),
                    Line("Em            C", "o sol pinta o meu lugar"),
                    Line("G                 D", "poeira no pé descalço"),
                    Line("Em            C     D", "levo a viola pra cantar"),
                )),
                Section("Refrão", listOf(
                    Line("    C          G", "e a estrada me chama"),
                    Line("   D           Em", "com cheiro de café"),
                    Line("  C           G", "o mundo é tão simples"),
                    Line("   D           G", "quando se anda a pé"),
                )),
                Section("Verso", listOf(
                    Line("G                D", "paro à sombra de um pé"),
                    Line("Em             C", "escuto o vento dizer"),
                    Line("G               D", "que a pressa não leva a nada"),
                    Line("Em          C    D", "só o tempo de viver"),
                )),
            )
        ),
        Song(
            id = "cafe-violao",
            file = "Café e Violão - Ana Caravelas.txt",
            title = "Café e Violão",
            artist = "Ana Caravelas",
            key = "C",
            capo = 0,
            genre = "MPB",
            ext = "txt",
            sections = listOf(
                Section("Intro", listOf(
                    Line("C   Cmaj7  F   G", "")
                )),
                Section("Verso", listOf(
                    Line("C              Am", "café esfriando na mesa"),
                    Line("Dm             G", "a tarde inteira pra nós"),
                    Line("C              Am", "um disco antigo tocando"),
                    Line("F        G      C", "e o silêncio entre a voz"),
                )),
                Section("Refrão", listOf(
                    Line("   F           G", "fica mais um pouco aqui"),
                    Line("   Em          Am", "o relógio pode esperar"),
                    Line("   F          G", "enquanto a chuva não vem"),
                    Line("  C        G   C", "deixa o som nos levar"),
                )),
            )
        ),
        Song(
            id = "vento-norte",
            file = "Vento Norte - Banda Litoral.txt",
            title = "Vento Norte",
            artist = "Banda Litoral",
            key = "D",
            capo = 2,
            genre = "Rock",
            ext = "txt",
            sections = listOf(
                Section("Intro", listOf(
                    Line("D  A  Bm  G", "")
                )),
                Section("Verso", listOf(
                    Line("D             A", "o vento norte chegou"),
                    Line("Bm            G", "trazendo notícia do mar"),
                    Line("D              A", "deixou meu cabelo bagunçado"),
                    Line("Bm        G    A", "e a vontade de viajar"),
                )),
                Section("Refrão", listOf(
                    Line("    G          D", "leva eu pra qualquer lugar"),
                    Line("    A          Bm", "onde o sol nasça primeiro"),
                    Line("   G           D", "que eu vou de mãos vazias"),
                    Line("   A           D", "e bolso aventureiro"),
                )),
            )
        ),
    )
}

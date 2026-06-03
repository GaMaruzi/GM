// songs.jsx — demo songbook (ORIGINAL lyrics, fictional artists — no copyrighted content)
// Exports to window: SONGS
// line format: { ch: "<chord line>", ly: "<lyric line>" } — ch or ly may be ""

const SONGS = [
  {
    id: 'manha-estrada',
    title: 'Manhã na Estrada',
    artist: 'Rio Vermelho',
    key: 'G',
    capo: 0,
    genre: 'Folk',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'G   D   Em  C', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'G             D', ly: 'A manhã chega devagar' },
        { ch: 'Em            C', ly: 'o sol pinta o meu lugar' },
        { ch: 'G                 D', ly: 'poeira no pé descalço' },
        { ch: 'Em            C     D', ly: 'levo a viola pra cantar' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '    C          G', ly: 'e a estrada me chama' },
        { ch: '   D           Em', ly: 'com cheiro de café' },
        { ch: '  C           G', ly: 'o mundo é tão simples' },
        { ch: '   D           G', ly: 'quando se anda a pé' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'G                D', ly: 'paro à sombra de um pé' },
        { ch: 'Em             C', ly: 'escuto o vento dizer' },
        { ch: 'G               D', ly: 'que a pressa não leva a nada' },
        { ch: 'Em          C    D', ly: 'só o tempo de viver' },
      ]},
    ],
  },
  {
    id: 'cafe-violao',
    title: 'Café e Violão',
    artist: 'Ana Caravelas',
    key: 'C',
    capo: 0,
    genre: 'MPB',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'C   Cmaj7  F   G', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'C              Am', ly: 'café esfriando na mesa' },
        { ch: 'Dm             G', ly: 'a tarde inteira pra nós' },
        { ch: 'C              Am', ly: 'um disco antigo tocando' },
        { ch: 'F        G      C', ly: 'e o silêncio entre a voz' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '   F           G', ly: 'fica mais um pouco aqui' },
        { ch: '   Em          Am', ly: 'o relógio pode esperar' },
        { ch: '   F          G', ly: 'enquanto a chuva não vem' },
        { ch: '  C        G   C', ly: 'deixa o som nos levar' },
      ]},
    ],
  },
  {
    id: 'luz-domingo',
    title: 'Luz de Domingo',
    artist: 'Coral Aurora',
    key: 'D',
    capo: 2,
    genre: 'Gospel',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'D   A   Bm  G', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'D              A', ly: 'a luz entra pela janela' },
        { ch: 'Bm             G', ly: 'aquece o chão onde piso' },
        { ch: 'D              A', ly: 'levanto a voz que estava' },
        { ch: 'G        A      D', ly: 'guardada para esse riso' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '  G            D', ly: 'e canto porque é manhã' },
        { ch: '  A            Bm', ly: 'porque há um novo chão' },
        { ch: '  G           D', ly: 'a noite ficou pra trás' },
        { ch: '   A          D', ly: 'e segue o coração' },
      ]},
    ],
  },
  {
    id: 'beira-mar',
    title: 'Beira-Mar',
    artist: 'Trio Maré',
    key: 'Em',
    capo: 0,
    genre: 'Pop',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'Em  C   G   D', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'Em             C', ly: 'a maré desenha no chão' },
        { ch: 'G              D', ly: 'o nome que eu não disse' },
        { ch: 'Em             C', ly: 'a espuma apaga depressa' },
        { ch: 'G          D    Em', ly: 'tudo que o mar me fez' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '   C          G', ly: 'volta pra beira do mar' },
        { ch: '   D          Em', ly: 'onde o tempo é maré' },
        { ch: '   C          G', ly: 'e o resto do mundo' },
        { ch: '   D          Em', ly: 'fica pequeno e de pé' },
      ]},
    ],
  },
  {
    id: 'velho-casarao',
    title: 'Velho Casarão',
    artist: 'Joaquim Brandão',
    key: 'Am',
    capo: 0,
    genre: 'Rock',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'Am  F   C   G', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'Am             F', ly: 'as portas rangem baixinho' },
        { ch: 'C              G', ly: 'guardam histórias no breu' },
        { ch: 'Am             F', ly: 'retratos cobertos de poeira' },
        { ch: 'C        G     Am', ly: 'de tudo que aqui viveu' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '   F          C', ly: 'velho casarão de pé' },
        { ch: '   G          Am', ly: 'guarda o que eu fui um dia' },
        { ch: '   F          C', ly: 'as paredes sabem mais' },
        { ch: '   G          Am', ly: 'do que a boca diria' },
      ]},
    ],
  },
  {
    id: 'pe-de-serra',
    title: 'Pé de Serra',
    artist: 'Banda Catavento',
    key: 'G',
    capo: 0,
    genre: 'Forró',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'G   C   D   G', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'G                C', ly: 'sanfona puxa a cantiga' },
        { ch: 'D              G', ly: 'poeira sobe no salão' },
        { ch: 'G                C', ly: 'a saia roda na valsa' },
        { ch: 'D          G', ly: 'do meu pé de serra, então' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '  C          G', ly: 'vem dançar comigo aqui' },
        { ch: '  D          G', ly: 'que a noite mal começou' },
        { ch: '  C          G', ly: 'no compasso do triângulo' },
        { ch: '  D          G', ly: 'ninguém ainda parou' },
      ]},
    ],
  },
  {
    id: 'noite-azul',
    title: 'Noite Azul',
    artist: 'Marina Solar',
    key: 'C',
    capo: 0,
    genre: 'Bossa',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'Cmaj7  Am7  Dm  G7', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'Cmaj7          Am7', ly: 'a noite chega de azul' },
        { ch: 'Dm             G7', ly: 'sem pressa de anoitecer' },
        { ch: 'Cmaj7          Am7', ly: 'a lua na taça vazia' },
        { ch: 'Dm        G7    C', ly: 'brindando o entardecer' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '   F          Em7', ly: 'fica, que a cidade dorme' },
        { ch: '   Dm         G7', ly: 'e o mar nem sabe de nós' },
        { ch: '   F          Em7', ly: 'a noite é só um pretexto' },
        { ch: '   Dm    G7    C', ly: 'pra ouvir a sua voz' },
      ]},
    ],
  },
  {
    id: 'caminho-volta',
    title: 'Caminho de Volta',
    artist: 'Os Tropeiros',
    key: 'D',
    capo: 0,
    genre: 'Country',
    sections: [
      { tag: 'Intro', lines: [
        { ch: 'D   G   A   D', ly: '' },
      ]},
      { tag: 'Verso', lines: [
        { ch: 'D              G', ly: 'a poeira no retrovisor' },
        { ch: 'A              D', ly: 'a cidade ficando pra trás' },
        { ch: 'D              G', ly: 'a viola no banco de trás' },
        { ch: 'A          D', ly: 'e a saudade que não se desfaz' },
      ]},
      { tag: 'Refrão', lines: [
        { ch: '   G          D', ly: 'caminho de volta pra casa' },
        { ch: '   A          D', ly: 'o sol se pondo no chão' },
        { ch: '   G          D', ly: 'cada curva é um abraço' },
        { ch: '   A          D', ly: 'que espera no portão' },
      ]},
    ],
  },
];

// Derive offline-file metadata so the app reads like it indexed a local folder.
SONGS.forEach((s) => {
  s.file = `${s.title} - ${s.artist}.txt`;
  s.ext = 'txt';
});

Object.assign(window, { SONGS });

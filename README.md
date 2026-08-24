# Street Combat — contrato WebSocket para o frontend

Este servidor usa **WebSocket puro com mensagens de texto JSON**. Ele **não usa STOMP**: não há `@MessageMapping`, `@SendTo`, destinos STOMP, filas nem tópicos para `subscribe`. O frontend deve manter uma única conexão WebSocket e receber todos os eventos diretamente pelo callback `onmessage`.

## Conexão

- Endpoint registrado pelo servidor: `/jogo`
- Host de produção: `https://servidor-street-combat.onrender.com`
- URL WebSocket completa: `wss://servidor-street-combat.onrender.com/jogo`
- Subprotocolo: nenhum. **Não** usar SockJS nem cliente STOMP.
- Formato de todas as mensagens: texto UTF-8 contendo um objeto JSON.

Exemplo:

```js
const socket = new WebSocket('wss://servidor-street-combat.onrender.com/jogo');
socket.onmessage = ({ data }) => {
  const evento = JSON.parse(data);
  // Decidir pelo campo evento.tipo.
};
```

Ao abrir a conexão, o servidor envia imediatamente `salasOnline` (não é preciso requisitá-lo antes). Guarde `idSessao`; ele identifica a sessão atual no servidor.

## Mensagens enviadas pelo frontend

Não há caminhos de envio como `/app/...`; o destino é sempre a própria conexão `/jogo`. A operação é selecionada pelo campo obrigatório `tipo`.

| `tipo` | Payload exato | Efeito |
|---|---|---|
| `criarSala` | `{ "tipo": "criarSala", "mapa": "cidade", "personagem": "feminimo", "nomeCriador": "Ana" }` | Cria uma sala cujo ID é exatamente `nomeCriador`; adiciona o cliente como jogador. Valores de mapa previstos: `cidade`, `prisão`, `quintal`. Somente `cidade` cria inimigos, itens e barreiras. Personagens previstos: `feminimo` e `masculino` (grafia do código). |
| `entrarSala` | `{ "tipo": "entrarSala", "idSala": "Ana", "personagem": "masculino", "nomeJogador": "Bruno" }` | Adiciona o cliente à sala existente. `idSala` deve ser um valor presente em `salasOnline.salas`. |
| `sairDaSala` | `{ "tipo": "sairDaSala" }` | Solicita saída da sala. |
| `receber_salas` | `{ "tipo": "receber_salas" }` | Solicita novamente a lista de salas. |
| `andar` | `{ "tipo": "andar", "x": 250, "y": 120, "direcao": "direita" }` | Envia posição absoluta e direção. `x` e `y` devem ser números inteiros; direções usadas pelo servidor: `direita` e `esquerda`. |
| `tentarAtacar` | `{ "tipo": "tentarAtacar" }` | Solicita a seleção aleatória de um golpe do personagem. |
| `golpearInimigo` | `{ "tipo": "golpearInimigo", "idInimigo": "<uuid>" }` | Aplica o dano do jogador ao inimigo de ID informado. |
| `levarDano` | `{ "tipo": "levarDano", "dano": 2 }` | Solicita que o jogador atual receba dano inteiro. |
| `golpearItem` | `{ "tipo": "golpearItem", "idItem": "<uuid>" }` | Aplica o dano do jogador ao item de ID informado. |
| `golpearBarreira` | `{ "tipo": "golpearBarreira", "idBarreira": "<uuid>" }` | Aplica o dano do jogador à barreira de ID informado. |

Os IDs de inimigos, itens e barreiras são as chaves UUID dos objetos recebidos nos eventos correspondentes. O servidor não valida esquema, limites, colisões nem permissões: o frontend deve tratar `x`, `y`, `dano` e IDs como dados confiáveis somente depois de validação própria.

## Mensagens recebidas do backend

Todos os eventos abaixo chegam pela mesma conexão. O campo `tipo` é o discriminador obrigatório. Campos Java serializados por getters Lombok aparecem em JSON em `camelCase`.

### Salas

```json
{
  "tipo": "salasOnline",
  "salas": ["Ana", "OutraSala"],
  "idSessao": "id-da-sessao-websocket"
}
```

É enviado na abertura. `salas` é um array de IDs de sala; hoje cada ID é o `nomeCriador` usado em `criarSala`.

```json
{
  "tipo": "salasOnline",
  "salas": ["Ana"]
}
```

É a resposta a `receber_salas`; nesta variante, `idSessao` não existe.

```json
{
  "tipo": "atualizarSalasOnline",
  "salas": ["Ana"]
}
```

É enviado após criar, entrar ou sair de uma sala. A implementação atual mantém as conexões também na lista global, portanto esse evento pode chegar inclusive a clientes já em uma sala.

### Jogadores

```json
{
  "tipo": "atualizarJogadores",
  "jogadores": {
    "Ana": {
      "id": "id-da-sessao-websocket",
      "nomeJogador": "Ana",
      "personagem": "feminimo",
      "direcao": "direita",
      "vida": 20,
      "vidaMaxima": 20,
      "x": 200,
      "y": 100,
      "dano": 5,
      "atributosExtras": []
    }
  }
}
```

É enviado a todos os participantes da sala ao entrar, mover, sofrer dano, receber/remover efeito ou sair. `jogadores` é um objeto/dicionário: a chave é `nomeJogador`, e não o campo interno `id`. `atributosExtras` contém zero ou mais valores: `DANO_EXTRA`, `DEFESA_EXTRA`, `RECUPERAR_VIDA`, `VELOCIDADE_EXTRA`, `VENENO`.

Vida e dano inicial variam pelo mapa: `cidade` = `vida`/`vidaMaxima` 20 e `dano` 5; `prisão` = 34 e 6; `quintal` = 42 e 8. Posições iniciais são `x: 200`, `y: 100`, direção inicial `direita`.

### Golpe do jogador

```json
{
  "tipo": "jogadorTentouGolpe",
  "golpe": "MARTELO_LONGO_ALCANCE",
  "idJogador": "id-da-sessao-websocket"
}
```

É enviado a todos os participantes quando `tentarAtacar` é processado. Para `feminimo`, `golpe` é um de `MARTELO_LONGO_ALCANCE`, `MARTELO_MEDIO_ALCANCE`, `MARTELO_CURTO_ALCANCE`, `SOCO_DIRETO`. Para `masculino`, é um de `CHUTE_NORMAL`, `CHUTE_BAIXO`, `SOCO_JAB`, `SOCO_DIRETO`, `SOCO_BAIXO`.

### Inimigos

```json
{
  "tipo": "atualizarInimigos",
  "inimigos": {
    "550e8400-e29b-41d4-a716-446655440000": {
      "nome": "david",
      "vida": 10,
      "dano": 2,
      "boss": false,
      "pontosPorDerrotar": 1,
      "x": 145,
      "y": 311,
      "direcao": "esquerda",
      "velocidade": 2,
      "quantidadeMovimentos": 5,
      "direcaoAleatoria": 0
    }
  }
}
```

É enviado aproximadamente a cada 200 ms para participantes da sala e após `golpearInimigo`. `inimigos` é um dicionário indexado por UUID. Em `cidade`, o mapa inicia com 3 `david`, 2 `raymond`, 2 `rick`, 2 `stella` e 1 `LucasLee`. As posições são aleatórias entre 100 e 500, inclusive. Quando um inimigo morre, sua chave deixa de existir. Todos os campos exibidos acima são serializados pelo servidor.

### Itens

```json
{
  "tipo": "atualizarItens",
  "itens": {
    "550e8400-e29b-41d4-a716-446655440001": {
      "nome": "recuperarVida",
      "tipo": "lixeira",
      "vida": 10,
      "x": 214,
      "y": 386
    }
  }
}
```

É enviado ao entrar na sala e após `golpearItem`. `itens` é um dicionário UUID → item. Em `cidade`, inicia com 2 `recuperarVida`, 2 `danoExtra`, 1 `escudoExtra`, 2 `veneno` e 3 `velocidadeExtra`. Ao ser destruído, o item é removido e o efeito correspondente é aplicado por 60 segundos: cura 5 a cada 5 s (`RECUPERAR_VIDA`), causa 1 de dano a cada 5 s (`VENENO`) ou apenas permanece como atributo (`DANO_EXTRA`, `DEFESA_EXTRA`, `VELOCIDADE_EXTRA`).

### Barreiras

```json
{
  "tipo": "atualizarBarreiras",
  "barreiras": {
    "550e8400-e29b-41d4-a716-446655440002": {
      "tipo": "barreira",
      "vida": 10,
      "x": 175,
      "y": 427
    }
  }
}
```

É enviado após `golpearBarreira`. `barreiras` é um dicionário UUID → barreira. Em `cidade`, há 4 inicialmente. A barreira é removida do dicionário quando `vida <= 0`.

## Fluxo recomendado

1. Abra `wss://servidor-street-combat.onrender.com/jogo` e instale o manipulador de mensagens **antes** de depender de dados do jogo.
2. Ao receber `salasOnline`, renderize `salas` e guarde `idSessao`.
3. Crie uma sala com `criarSala` ou entre em uma existente com `entrarSala`.
4. Processe imediatamente `atualizarJogadores` e `atualizarItens`. Para `cidade`, também espere atualizações periódicas de `atualizarInimigos`.
5. Para renderizar o mundo, substitua o estado local de cada coleção pelo dicionário inteiro recebido em cada evento.
6. Envie ações para a mesma conexão usando os JSONs desta documentação. Pegue `idInimigo`, `idItem` e `idBarreira` das chaves dos dicionários mais recentes.
7. Ao encerrar a partida, envie `sairDaSala` e feche o socket.

## Limitações importantes da implementação atual

- Não há confirmação de sucesso, nem evento de erro, para criar/entrar/sair ou ações inválidas. Se `idSala` não existir, `entrarSala` não produz resposta específica.
- O mapa `jogadores` é gravado com chave `nomeJogador`, mas os métodos de mover, atacar, sofrer dano e remover procuram a chave pelo ID da sessão. Assim, com nomes normais, essas ações podem não localizar o jogador e não gerar o evento esperado. Esta documentação descreve o comportamento do código como está; o frontend não consegue corrigir essa inconsistência por protocolo.
- `itemEstaVivo` retorna o estado de morte; por isso o efeito e a remoção do item ocorrem apenas quando sua vida chega a zero ou menos, apesar do nome do método.
- Somente `cidade` popula entidades. `prisão` e `quintal` criam uma sala sem inimigos, itens ou barreiras.

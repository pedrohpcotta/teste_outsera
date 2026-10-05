# 02 — Clarificações

Pontos ambíguos ou não cobertos pela especificação, levantados pela IA durante a análise, e a decisão tomada em cada um. O item 5 foi uma definição minha, passada junto com as respostas.

## Decisões de produto e projeto

| # | Questão | Decisão |
|---|---|---|
| 1 | Se o mesmo produtor vence dois filmes no mesmo ano, isso conta como intervalo `0`? | Sim, conta |
| 2 | Além do endpoint obrigatório, expor a listagem de filmes (`GET /movies`, `GET /movies/{id}`)? | Sim. Cobre a "leitura da lista de indicados e vencedores" e reforça o nível 2 de Richardson |
| 3 | Maven ou Gradle? | Maven, com Maven Wrapper |
| 4 | Idioma da documentação? | Português. Identificadores no código em inglês |
| 5 | Comentários no código? | Não usar |

## Casos de borda e comportamento definido

| Caso | Comportamento |
|---|---|
| Vários produtores com o mesmo intervalo mínimo ou máximo | Todos são retornados, ordenados por nome e ano |
| Mesmo produtor com o menor e o maior intervalo | Aparece em `min` e em `max` |
| Produtor com três ou mais vitórias | Cada par de vitórias consecutivas gera um intervalo |
| Nenhum produtor com duas vitórias | `200` com `{"min": [], "max": []}` |
| Apenas um intervalo no conjunto | O mesmo item aparece em `min` e em `max` |
| `winner` com variação de caixa ou espaços (`YES`, ` yes `) | Considerado vencedor |
| Indicações não vencedoras | Ignoradas no cálculo |
| Separador ` and ` em maiúsculas ou misto (` AND `, ` And `) | Também separa produtores |
| Mesmo nome com caixa diferente (`Joel Silver` e `joel silver`) | Mesmo produtor ou estúdio; prevalece a primeira grafia do arquivo |
| Nomes contendo "and" (ex.: `Anderson`) | Não são divididos: o separador exige espaço antes e depois |
| Estúdios | Separados apenas por vírgula, para não quebrar nomes com "and" |
| Arquivo UTF-8 com BOM (salvo pelo Excel ou no Windows) | Aceito: o BOM é descartado antes da leitura do cabeçalho |
| CSV inválido (arquivo inexistente, cabeçalho ausente, ano não numérico, título vazio, colunas inconsistentes, aspas não fechadas) | A aplicação não inicia e informa o problema e a linha correspondente |

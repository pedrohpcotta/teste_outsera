# 04 — Log de interações

Registro cronológico das interações com o agente (Claude Code) que tiveram impacto no projeto. Os prompts estão transcritos com a grafia corrigida.

---

## 1. Contexto inicial

**Prompt:** `/init`

**Resultado:** com a pasta contendo apenas o PDF da avaliação e o CSV, o agente leu os dois e gerou um `CLAUDE.md` inicial, um arquivo local de contexto para o agente. Esse arquivo trazia os requisitos, o formato de resposta esperado e as particularidades do CSV: separador `;`, `winner` vazio para indicados e produtores separados por `,` e ` and `.

---

## 2. Análise da especificação e debate

**Prompt:**
> Leia o arquivo Avaliação - Back-end.pdf e vamos debater a respeito. A ideia é construir algo no backend nas APIs, com Java 21 e Spring Boot, Spring REST, Spring Data etc. Entenda o contexto dos requisitos e me dê sugestões de como fazer.

**Resultado:**
- Identificação do ponto mais crítico da avaliação: o resultado precisa estar correto para outros conjuntos de dados. Por isso o caminho do CSV deve ser configurável.
- Cálculo do resultado esperado a partir do CSV (Joel Silver: 1; Matthew Vaughn: 13), usado depois como asserção do teste principal.
- Proposta de stack, modelo de dados, algoritmo e estratégia de testes só de integração, usando datasets de cenário.
- Levantamento de casos de borda: anos com mais de um vencedor (1986, 1990, 2015), mesmo produtor vencendo duas vezes no mesmo ano, nenhum intervalo, separadores mistos.
- Quatro perguntas de decisão (ver [02-clarificacoes.md](02-clarificacoes.md)).

**Revisão:** avaliei a proposta e respondi às decisões em aberto.

---

## 3. Decisões e início da implementação

**Prompt:**
> 1 - sim; 2 - acho que seria bom incluirmos; 3 - maven; 4 - português. Além disso, não coloque comentários no código. De resto, pode seguir com as implementações de acordo com o que definimos aqui.

**Resultado:**
- Projeto criado conforme o [plano](03-plano-e-tarefas.md): entidades, carga do CSV, endpoint de intervalos, endpoints de filmes e tratamento de erros.
- Ajuste feito durante a implementação: ao analisar o CSV, o agente separou as regras de divisão de nomes. Produtores são divididos por `,` e ` and `; estúdios, apenas por `,`.
- Seis CSVs de cenário criados em `src/test/resources/datasets/`.

**Problemas encontrados na primeira execução dos testes e corrigidos:**

| Sintoma | Causa | Correção |
|---|---|---|
| Testes de `/movies` recebiam os dados de outro dataset (10 filmes em vez de 206) | Com uma URL fixa do H2, os contextos de teste com propriedades diferentes compartilhavam o mesmo banco, e a última carga sobrescrevia as anteriores | `spring.datasource.generate-unique-name: true`, um banco por contexto |
| Testes de cenário recebiam o resultado do dataset oficial | As classes `@Nested` usavam o `MockMvc` injetado na classe externa, que pertence ao contexto padrão | Cada classe aninhada passou a injetar o próprio `MockMvc` |

**Verificação:**
- 13 testes de integração passando.
- Aplicação executada via jar, com chamadas reais aos endpoints:
  - `/producers/award-intervals` com o CSV oficial;
  - `/producers/award-intervals` com um CSV externo, via `--app.movies.csv-path=file:...`;
  - `404` em `/movies/{id}` inexistente;
  - listagem filtrada de vencedores.
- Confirmado que `./mvnw test -Dtest=ProducerAwardIntervalsIntegrationTest` executa também as classes aninhadas, como documentado no README.

---

## 4. Registro do uso de IA

**Prompt:**
> Me mostre como ficariam os logs, por gentileza.

**Resultado:** organização deste registro no formato de Spec-Driven Development (especificação, clarificações, plano/tarefas e log), a partir das interações reais do desenvolvimento.

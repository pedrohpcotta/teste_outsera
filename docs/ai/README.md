# Uso de IA no desenvolvimento

- **Ferramenta:** Claude Code (modelo Claude Opus), via terminal.
- **Abordagem:** Spec-Driven Development. A implementação só começou depois que a especificação, os critérios de aceite e as decisões em aberto estavam fechados.

## Fluxo

| Etapa | Artefato | Papel da IA | Meu papel |
|---|---|---|---|
| Contexto | `CLAUDE.md` (local, não versionado) | Gerou via `/init` e manteve atualizado | Revisar |
| Especificação | [01-especificacao.md](01-especificacao.md) | Extraiu requisitos do PDF e analisou o CSV | Revisar e validar |
| Clarificação | [02-clarificacoes.md](02-clarificacoes.md) | Levantou ambiguidades e casos de borda | Decidir |
| Plano | [03-plano-e-tarefas.md](03-plano-e-tarefas.md) | Propôs arquitetura e tarefas | Aprovar |
| Implementação | `src/` | Implementou código e testes | Acompanhar |
| Verificação | testes + execução manual | Executou, corrigiu falhas | Validar resultado |

O histórico das interações está em [04-log-de-interacoes.md](04-log-de-interacoes.md).

## Skills e comandos utilizados

- `/init`: geração do `CLAUDE.md`, arquivo local com o contexto do projeto para o agente.
- Execução de comandos pelo agente: build e testes (`./mvnw test`), execução da aplicação e chamadas `curl` para validar os endpoints.
- Script auxiliar em Python, usado só durante a análise, para calcular o resultado esperado a partir do CSV antes de existir qualquer código. Esse resultado virou a asserção do teste de integração principal.

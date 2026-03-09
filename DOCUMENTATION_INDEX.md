# 📚 Índice de Documentação GAMA

**Última atualização:** 2026-03-09  
**Versão GAMA:** 1.2.2

Este índice organiza toda a documentação do projeto GAMA para facilitar a navegação.

---

## 🚀 Para Começar Rapidamente

Primeiro acesso? Comece aqui:

1. **[QUICK_START.md](QUICK_START.md)** ⭐ 
   - Guia rápido para usuários e desenvolvedores
   - Instruções simples e diretas
   - Comandos essenciais

2. **[README.md](README.md)**
   - Visão geral do projeto
   - Descrição completa do sistema
   - Funcionalidades principais

---

## 📦 Para Criar e Distribuir Pacotes

### Scripts Disponíveis

- **`build-package.ps1`** - Script PowerShell para criar pacote standalone
- **`build-package.bat`** - Script CMD/Batch (alternativa)
- **`build-installer.ps1`** - Script para criar instalador MSI
- **`validate-package.ps1`** - Script para validar pacote antes de distribuir

### Documentação de Distribuição

3. **[DISTRIBUTION.md](DISTRIBUTION.md)** ⭐
   - Documentação técnica completa
   - Como a solução funciona
   - Comparação antes vs depois
   - Estrutura do pacote gerado
   - Troubleshooting detalhado

4. **[EXAMPLES.md](EXAMPLES.md)** ⭐
   - 12 cenários práticos de uso
   - Scripts de exemplo
   - Casos de uso reais
   - Validação e debugging

5. **[DISTRIBUTION_CHECKLIST.md](DISTRIBUTION_CHECKLIST.md)**
   - Checklist completo para releases
   - 8 fases de distribuição
   - Template de release notes
   - Plano de rollback

---

## 🔧 Para Desenvolvedores

### Desenvolvimento

- **[QUICK_START.md](QUICK_START.md)** - Seção "Para Desenvolvedores"
  - Como executar em modo desenvolvimento
  - Comandos Maven úteis
  - Configuração de IDEs

- **[EXAMPLES.md](EXAMPLES.md)** - Cenários 1, 6, 8, 9, 12
  - Desenvolvimento local
  - CI/CD
  - Debug
  - Builds customizados

### Arquitetura e Código

- **[README.md](README.md)** - Seções técnicas
  - Dependências do projeto
  - Estrutura de funcionalidades
  - Tecnologias utilizadas

- **Código Fonte:** `src/main/java/com/digitallib/`
  - Veja estrutura em anexo do contexto

---

## 📋 Para Mantenedores e Release Managers

### Processo de Release

1. **[DISTRIBUTION_CHECKLIST.md](DISTRIBUTION_CHECKLIST.md)** ⭐
   - Checklist passo a passo
   - 8 fases completas
   - Validação e testes

2. **[EXAMPLES.md](EXAMPLES.md)** - Cenários 3, 4, 7, 10, 11
   - Distribuição para usuários
   - Criar instaladores
   - Validação de pacotes
   - Documentação de versão

3. **[DISTRIBUTION.md](DISTRIBUTION.md)**
   - Detalhes técnicos
   - Resolução de problemas
   - Comparação de métodos

### Manutenção

- **[OBSOLETE_FILES.md](OBSOLETE_FILES.md)**
  - Arquivos não mais necessários
  - Guia de limpeza
  - Migração para novo sistema

- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)**
  - Resumo completo da implementação
  - O que foi feito e por quê
  - Benefícios alcançados

---

## 👥 Para Usuários Finais

### Instalação e Uso

- **[QUICK_START.md](QUICK_START.md)** - Seção "Para Usuários Finais"
  - Instruções de instalação simples
  - 3 passos apenas

- **[README.md](README.md)** - Seção "Uso"
  - Como usar a aplicação
  - Funcionalidades principais
  - Filtros e exportação

### Solução de Problemas

- **[QUICK_START.md](QUICK_START.md)** - Seção "Solução de Problemas"
  - Problemas comuns
  - Soluções rápidas

- **[DISTRIBUTION.md](DISTRIBUTION.md)** - Seção "Resolução de Problemas"
  - Troubleshooting detalhado
  - Erros técnicos

---

## 📖 Documentos por Categoria

### Guias Essenciais

| Documento | Público-Alvo | Descrição |
|-----------|--------------|-----------|
| [QUICK_START.md](QUICK_START.md) | Todos | Guia rápido para começar |
| [README.md](README.md) | Todos | Documentação principal do projeto |
| [DISTRIBUTION.md](DISTRIBUTION.md) | Desenvolvedores | Detalhes técnicos de distribuição |

### Guias Práticos

| Documento | Público-Alvo | Descrição |
|-----------|--------------|-----------|
| [EXAMPLES.md](EXAMPLES.md) | Desenvolvedores | 12 cenários práticos |
| [DISTRIBUTION_CHECKLIST.md](DISTRIBUTION_CHECKLIST.md) | Mantenedores | Checklist de release |
| [OBSOLETE_FILES.md](OBSOLETE_FILES.md) | Desenvolvedores | Arquivos obsoletos |

### Referência e Histórico

| Documento | Público-Alvo | Descrição |
|-----------|--------------|-----------|
| [LICENSE](LICENSE) | Todos | Licença GNU AGPL v3.0 |

---

## 🔍 Encontre por Tarefa

### "Quero executar o GAMA localmente"
→ [QUICK_START.md](QUICK_START.md) - Seção "Executar em Modo Desenvolvimento"

### "Quero criar um pacote para distribuir"
→ [QUICK_START.md](QUICK_START.md) - Seção "Criar Pacote para Distribuição"  
→ [EXAMPLES.md](EXAMPLES.md) - Cenário 2 e 3

### "Quero entender como funciona"
→ [DISTRIBUTION.md](DISTRIBUTION.md) - Seção "O que mudou?"

### "Quero ver exemplos práticos"
→ [EXAMPLES.md](EXAMPLES.md) - Todos os 12 cenários

### "Quero fazer um release"
→ [DISTRIBUTION_CHECKLIST.md](DISTRIBUTION_CHECKLIST.md) - Checklist completo

### "Quero validar o pacote"
→ [EXAMPLES.md](EXAMPLES.md) - Cenário 10  
→ Executar: `.\validate-package.ps1`

### "Encontrei um problema"
→ [DISTRIBUTION.md](DISTRIBUTION.md) - Seção "Resolução de Problemas"  
→ [EXAMPLES.md](EXAMPLES.md) - Seção "Troubleshooting Comum"  
→ [JAVAFX_FIX.md](JAVAFX_FIX.md) - Correção do erro "Module javafx.base not found"

### "Quero configurar CI/CD"
→ [EXAMPLES.md](EXAMPLES.md) - Cenário 6

### "Quero limpar arquivos antigos"
→ [OBSOLETE_FILES.md](OBSOLETE_FILES.md) - Seção "Comandos de Limpeza"

---

## 📊 Fluxograma de Documentação

```
Começar
   ↓
[Usuário Final?]
   ├─ Sim → QUICK_START.md (seção usuário)
   │         ↓
   │      Problemas?
   │         ↓
   │      DISTRIBUTION.md (troubleshooting)
   │
   ↓
[Desenvolvedor?]
   ├─ Sim → [Primeira vez?]
   │         ├─ Sim → QUICK_START.md
   │         │         ↓
   │         │      README.md (visão geral)
   │         │         ↓
   │         │      DISTRIBUTION.md (detalhes)
   │         │
   │         ↓
   │      [Quer exemplos?]
   │         ↓
   │      EXAMPLES.md (12 cenários)
   │
   ↓
[Fazer Release?]
   ├─ Sim → DISTRIBUTION_CHECKLIST.md
   │         ↓
   │      EXAMPLES.md (cenários 3, 4, 10)
   │         ↓
   │      validate-package.ps1
   │
   ↓
[Manutenção?]
   ├─ Sim → OBSOLETE_FILES.md
            ↓
         IMPLEMENTATION_SUMMARY.md
```

---

## 🎯 Cenários Comuns

### Cenário 1: Novo Desenvolvedor
1. [QUICK_START.md](QUICK_START.md) - Entender o projeto
2. [README.md](README.md) - Visão geral completa
3. [EXAMPLES.md](EXAMPLES.md) - Cenário 1 (Desenvolvimento Local)

### Cenário 2: Criar Build pela Primeira Vez
1. [DISTRIBUTION.md](DISTRIBUTION.md) - Entender a solução
2. [EXAMPLES.md](EXAMPLES.md) - Cenário 2 (Criar Pacote)
3. Executar: `.\build-package.ps1`
4. [EXAMPLES.md](EXAMPLES.md) - Cenário 10 (Validar)

### Cenário 3: Fazer Release Oficial
1. [DISTRIBUTION_CHECKLIST.md](DISTRIBUTION_CHECKLIST.md) - Seguir checklist
2. [EXAMPLES.md](EXAMPLES.md) - Cenário 3 e 4 (Distribuição)
3. [EXAMPLES.md](EXAMPLES.md) - Cenário 11 (Documentação)

### Cenário 4: Resolver Problema
1. [DISTRIBUTION.md](DISTRIBUTION.md) - Seção "Resolução de Problemas"
2. [EXAMPLES.md](EXAMPLES.md) - Seção "Troubleshooting Comum"
3. [QUICK_START.md](QUICK_START.md) - Seção "Solução de Problemas"

---

## 📞 Recursos Adicionais

### Externos
- [JavaFX Documentation](https://openjfx.io/)
- [jpackage Documentation](https://docs.oracle.com/en/java/javase/17/jpackage/)
- [Maven Documentation](https://maven.apache.org/)
- [WiX Toolset](https://wixtoolset.org/) (para MSI)

### Internos
- `pom.xml` - Configuração do Maven
- `classes.yaml` - Configuração de classes
- `src/` - Código fonte
- Scripts `.ps1` e `.bat` na raiz

---

## 🔄 Histórico de Mudanças

### 2026-03-09 - Correção do Erro JavaFX
- ✅ Corrigido erro "Module javafx.base not found"
- ✅ Script agora baixa JavaFX SDK automaticamente
- ✅ JavaFX adicionado manualmente ao pacote (evita jlink)
- ✅ Documentação JAVAFX_FIX.md criada

### 2026-03-09 - Implementação da Opção 2
- ✅ Criados scripts de build automatizados
- ✅ JavaFX agora embutido no pacote
- ✅ Documentação completa adicionada
- ✅ 11 arquivos novos criados
- ✅ README.md atualizado

---

## 💡 Dicas

- 📌 **Marque** [QUICK_START.md](QUICK_START.md) e [EXAMPLES.md](EXAMPLES.md) como favoritos
- 🔖 **Consulte** [DISTRIBUTION_CHECKLIST.md](DISTRIBUTION_CHECKLIST.md) antes de cada release
- 📝 **Atualize** este índice ao adicionar nova documentação
- 🎯 **Use** `validate-package.ps1` sempre antes de distribuir

---

**Este índice cobre toda a documentação GAMA.** Se algo estiver faltando, consulte a estrutura de diretórios do projeto.

**Atalho rápido:**
- Executar: `mvn javafx:run`
- Criar pacote: `.\build-package.ps1`
- Validar: `.\validate-package.ps1`


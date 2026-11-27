#!/bin/bash
# Script de teste para validar a migração de AUTOR para PESSOA com tratamento de ID duplicada

echo "========================================"
echo "TESTE DE MIGRAÇÃO AUTOR -> PESSOA"
echo "========================================"

# Criar diretório de teste
mkdir -p test_migration/repo/entities
cd test_migration

# Teste 1: Migração simples (sem conflito)
echo ""
echo "[Teste 1] Criando arquivo AUTOR simples..."
cat > repo/entities/1.json << 'EOF'
{"type":"AUTOR","name":"Fernando Pessoa","description":"Poeta português","id":"1"}
EOF

echo "Arquivo 1.json criado com AUTOR"
cat repo/entities/1.json
echo ""

# Teste 2: Migração com conflito de ID
echo "[Teste 2] Criando PESSOA com ID 2..."
cat > repo/entities/2.json << 'EOF'
{"type":"PESSOA","name":"João Silva","description":"Personagem","id":"2"}
EOF

echo "Arquivo 2.json criado com PESSOA"
cat repo/entities/2.json
echo ""

echo "[Teste 2] Criando arquivo AUTOR que será renomeado..."
cat > repo/entities/2_old.json << 'EOF'
{"type":"AUTOR","name":"Maria Antonieta","description":"Escritora","id":"2"}
EOF

echo "Arquivo 2_old.json criado com AUTOR (ID conflita com PESSOA ID 2)"
echo ""

echo "========================================"
echo "Para testar a migração:"
echo "1. Compile o projeto: mvn clean compile"
echo "2. Execute a aplicação"
echo "3. Verifique:"
echo "   - repo/entities/1.json deve ter 'type':'PESSOA'"
echo "   - repo/entities/2.json deve estar intacto (PESSOA original)"
echo "   - repo/entities/3.json deve ter PESSOA migrada (antes era AUTOR com ID 2)"
echo "   - repo/entities/.migration_completed deve existir"
echo "   - repo/entities/.backup_pre_migration/ deve ter backups"
echo "========================================"


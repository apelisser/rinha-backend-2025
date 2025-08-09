#!/bin/bash

# ====== Lê parâmetros ======
while getopts "y:" opt; do
  case $opt in
    y) YEAR="$OPTARG" ;;
    *) echo "Uso: $0 -y <ano>"; exit 1 ;;
  esac
done

if [ -z "$YEAR" ]; then
  echo "Erro: parâmetro -y <ano> é obrigatório."
  exit 1
fi

# ====== Extrai versão do pom.xml ======
echo "Buscando a versão do projeto no pom.xml..."
VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
echo "Versão encontrada: $VERSION"

# ====== Define nome base da imagem ======
IMAGE_NAME="rinha-backend-${YEAR}"

# ====== Build ======
echo "Construindo a imagem Docker: $IMAGE_NAME:$VERSION"
docker build -t "$IMAGE_NAME:$VERSION" .

# ====== Tags extras ======
echo "Adicionando a tag 'latest' para a imagem."
docker tag "$IMAGE_NAME:$VERSION" "$IMAGE_NAME:latest"

echo "Build concluído com sucesso!"

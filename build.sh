#!/bin/bash

# Extrai a versão do pom.xml usando o help:evaluate do Maven
# -q é para modo "quiet" para não poluir a saída
echo "Buscando a versão do projeto no pom.xml..."
VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
echo "Versão encontrada: $VERSION"

# Define o nome da imagem
IMAGE_NAME="rinha-2025-backend"

# Constrói a imagem Docker com a tag da versão
echo "Construindo a imagem Docker: $IMAGE_NAME:$VERSION"
docker build -t "$IMAGE_NAME:$VERSION" .

# (Opcional) Adiciona a tag 'latest' para conveniência
echo "Adicionando a tag 'latest' para a imagem."
docker tag "$IMAGE_NAME:$VERSION" "$IMAGE_NAME:latest"

echo "Build concluído com sucesso!"

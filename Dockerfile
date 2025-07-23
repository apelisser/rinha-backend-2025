# ======================================================================
# Estágio 1: Build - Compilar a aplicação para um executável nativo
# ======================================================================

# Usar imagem que contém o JDK e as ferramentas do GraalVM
FROM ghcr.io/graalvm/native-image-community:21-muslib AS builder

# Definir o diretório de trabalho
WORKDIR /app

# Copiar os arquivos de configuração do Maven primeiro para aproveitar o cache do Docker
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Baixar as dependências
RUN ./mvnw dependency:go-offline

# Copia o código fonte
COPY src/ ./src

# Executar o build do Maven ativando o perfil 'native' - isso vai gerar o executável nativo em /app/target/
RUN ./mvnw -Pnative native:compile -DskipTests


# ======================================================================
# Estágio 2: Final - Cria a imagem final
# ======================================================================

# Usar imagem "distroless/static" que não tem shell, nem gerenciador de pacotes, nem glibc.
FROM gcr.io/distroless/static-debian12

# Definir o diretório de trabalho
WORKDIR /app

# Copiar apenas o executável gerado no estágio anterior
COPY --from=builder /app/target/rinha2025 ./rinha2025

# Expor a porta que a aplicação vai usar
EXPOSE 8080

# Definir o comando para executar a aplicação quando o contêiner iniciar
ENTRYPOINT ["./rinha2025"]

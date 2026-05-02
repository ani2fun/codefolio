FROM node:22-alpine

# C/C++, Python, Go, SQLite
RUN apk add --no-cache \
    python3 \
    gcc g++ make musl-dev \
    go \
    openjdk21-jdk \
    sqlite

# TypeScript + ts-node
RUN npm install -g typescript tsx @types/node

WORKDIR /app
COPY server.js .

EXPOSE 2358
CMD ["node", "server.js"]

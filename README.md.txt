# ✂️ Sistema de Agendamento - Agenda.Fácil

Este é um projeto Full-Stack de um sistema de agendamento completo, ideal para barbearias, salões de beleza, clínicas ou qualquer negócio que opere com horários marcados.

O projeto foi construído com foco em segurança, regras de negócio claras e uma arquitetura moderna, separando o Backend (API RESTful) do Frontend (Aplicação React SPA).

## ✨ Funcionalidades Principais

O sistema possui 4 níveis de acesso (Master, Dono, Gerente, Profissional) além do Cliente, cada um com seu respectivo Dashboard e permissões.

### 🔑 Autenticação e Segurança
* **Login com JWT:** Autenticação segura usando JSON Web Tokens (expira em 2 horas).
* **Rate Limiting (Proteção de Login):** Bloqueio automático de conta por 5 minutos após 5 tentativas falhas de login.
* **Criptografia de Senhas:** Senhas armazenadas com hash usando BCrypt.
* **Segurança por Hierarquia (Roles):** Endpoints protegidos por nível de usuário usando `@PreAuthorize`.
* **IDs UUID:** Todos os identificadores de banco de dados usam UUIDs para evitar enumeração de recursos (IDOR).

### 👤 Painel do Cliente (DashboardCliente)
* **Agendamento Inteligente:** O cliente seleciona Profissional, Serviço(s) e Dia. O sistema calcula a duração total e busca *apenas* os horários disponíveis na API.
* **Visão em Calendário:** Visualização de seus agendamentos futuros em um calendário interativo (FullCalendar).
* **Gerenciamento:** Capacidade de listar e **Cancelar** seus próprios agendamentos.

### 🛠️ Painel do Profissional (DashboardProfissional)
* **Visualização da Própria Agenda:** Acesso rápido ao seu calendário de trabalho.
* **Gerenciamento de Status:** Permissão para marcar seus agendamentos como **Concluído** ou **Cancelado**.

### 🚀 Painel de Administração (DashboardAdmin)
* **Gestão de Equipe:** CRUD completo para Colaboradores (Donos, Gerentes, Profissionais), respeitando a hierarquia (Master cria Dono, Dono cria Gerente, etc.).
* **Gestão de Serviços:** CRUD completo para o catálogo de serviços (preço, duração, nome).
* **Gestão da Agenda Geral:** Visualização e gerenciamento (Cancelamento/Conclusão) de **todos** os agendamentos do sistema.
* **Configurações do Estabelecimento:** Definição dos horários de abertura/fechamento e dias da semana de funcionamento.

---

## 💻 Stack Tecnológica

### Backend (Java / Spring Boot)
* **Java 21**
* **Spring Boot 3.5.7**
* **Spring Security 6** (Autenticação JWT e autorização baseada em Roles)
* **Spring Data JPA (Hibernate)**
* **Banco de Dados (Dev):** H2 (Em memória)
* **Validação:** Jakarta Bean Validation
* **Build Tool:** Maven

### Frontend (React / Vite)
* **React 19** (com Hooks)
* **Vite** (Build tool e Dev Server)
* **React Router 7** (Roteamento de páginas)
* **Axios** (Requisições HTTP e Interceptors para JWT)
* **FullCalendar.io** (Visualização do calendário)
* **jwt-decode** (Para extrair o "Role" do token no frontend)

---

## 🚀 Como Executar Localmente

Você precisará ter o **JDK 21** (ou superior) e o **Node.js** (v18+) instalados.

### 1. Executando o Backend (API)

1.  Abra a pasta `backend/` no IntelliJ ou sua IDE Java preferida.
2.  Deixe o Maven baixar as dependências (do `pom.xml`).
3.  Execute o arquivo principal `ApiApplication.java`.
4.  O servidor Backend estará rodando em `http://localhost:8080`.
5.  O `DataSeeder` irá popular o banco H2 com usuários de teste (master, dono, profissionais, cliente) e serviços.

### 2. Executando o Frontend (React)

1.  Abra a pasta `frontend/` no VSCode ou seu editor de preferência.
2.  Abra um terminal e rode `npm install` para baixar as dependências (do `package.json`).
3.  Após a instalação, rode `npm run dev`.
4.  A aplicação React estará disponível em `http://localhost:5173` (ou outra porta indicada pelo Vite).
# Architecture
```mermaid
flowchart TD
 U[User] --> J[Java Console Application]
 J --> D[JDBC Driver]
 D --> P[(PostgreSQL)]
 P --> C[CSV Export]
 C --> PY[Python and Pandas]
 PY --> M[Matplotlib Charts]
```

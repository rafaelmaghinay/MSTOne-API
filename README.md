# MST ONE API

Spring Boot API for the MST ONE ERP system. The application uses Supabase
PostgreSQL as its database and Flyway for versioned schema migrations.

## Local database setup

1. Copy `.env.example` to `.env` as a reference, then configure those
   variables in your IDE or shell. Spring Boot does not load `.env` files
   automatically.
2. In the Supabase dashboard, open **Project Settings > Database** and copy a
   connection string. Convert it to a JDBC URL by prefixing it with
   `jdbc:` and append `sslmode=require`.
3. Set `SUPABASE_DB_USER` and `SUPABASE_DB_PASSWORD` from the same Supabase
   connection details.
4. Export those variables in the shell that starts Maven, then run:

   ```text
   .\mvnw.cmd spring-boot:run
   ```

The first startup applies `V1__create_core_hr_tables.sql` automatically.
Check the connection with `http://localhost:8080/actuator/health`.

Never commit `.env`, database passwords, Supabase service-role keys, or the
contents of the `database pass` directory. The API connects directly to
PostgreSQL through the server; Supabase service-role keys must not be used as
database credentials or exposed to an Angular client.

## Schema changes

Add a new numbered migration under
`src/main/resources/db/migration` for every schema change. Do not edit a
migration after it has been applied to a shared Supabase project.

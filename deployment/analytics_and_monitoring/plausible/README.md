<p align="center">
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/plausible/community-edition/refs/heads/v2.1.1/images/logo_dark.svg" width="300">
        <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/plausible/community-edition/refs/heads/v2.1.1/images/logo_light.svg" width="300">
        <img src="https://raw.githubusercontent.com/plausible/community-edition/refs/heads/v2.1.1/images/logo_light.svg" width="300">
    </picture>
</p>

<p align="center">
    A getting started guide to self-hosting <a href="https://plausible.io/blog/community-edition">Plausible Community Edition</a>
</p>

---

### Prerequisites

- **[Docker](https://docs.docker.com/engine/install/)** and **[Docker Compose](https://docs.docker.com/compose/install/)** must be installed on your machine.
- **CPU** must support **SSE 4.2** or **NEON** instruction set or higher (required by ClickHouse).
- At least **2 GB of RAM** is recommended for running ClickHouse and Plausible without fear of OOMs.

### Quick start

1. Clone this repository:

    ```console
    $ git clone -b v3.0.1 --single-branch https://github.com/plausible/community-edition plausible-ce
    Cloning into 'plausible-ce'...
    remote: Enumerating objects: 13, done.
    remote: Counting objects: 100% (10/10), done.
    remote: Compressing objects: 100% (9/9), done.
    remote: Total 13 (delta 0), reused 7 (delta 0), pack-reused 3 (from 1)
    Receiving objects: 100% (13/13), done.

    $ cd plausible-ce

    $ ls -1
    README.md
    clickhouse/
    compose.yml
    ```

1. Create and configure your [environment](https://docs.docker.com/compose/environment-variables/) file:

    ```console
    $ touch .env
    $ echo "BASE_URL=https://plausible.example.com" >> .env
    $ echo "SECRET_KEY_BASE=$(openssl rand -base64 48)" >> .env
    
    $ cat .env
    BASE_URL=https://plausible.example.com
    SECRET_KEY_BASE=As0fZsJlUpuFYSthRjT5Yflg/NlxkFKPRro72xMLXF8yInZ60s6xGGXYVqml+XN1
    ```

    Make sure `$BASE_URL` is set to the actual domain where you plan to host the service. The domain must have a DNS entry pointing to your server for proper resolution and automatic Let's Encrypt TLS certificate issuance. More on that in the next step.

1. Expose Plausible server to the web with a [compose override file:](https://github.com/plausible/community-edition/wiki/compose-override)

    ```sh
    $ echo "HTTP_PORT=80" >> .env
    $ echo "HTTPS_PORT=443" >> .env

    $ cat > compose.override.yml << EOF
    services:
      plausible:
        ports:
          - 80:80
          - 443:443
    EOF 
    ```

    Setting `HTTP_PORT=80` and `HTTPS_PORT=443` enables automatic Let's Encrypt TLS certificate issuance. You might want to choose different values if, for example, you plan to run Plausible behind [a reverse proxy.](https://github.com/plausible/community-edition/wiki/reverse-proxy)

1. Start the services with Docker Compose:

    ```console
    $ docker compose up -d
    ```

1. Visit your instance at `$BASE_URL` and create the first user.

> [!NOTE]
> Plausible CE is funded by our cloud subscribers.
>
> If you know someone who might [find Plausible useful](https://plausible.io/?utm_medium=Social&utm_source=GitHub&utm_campaign=readme), we'd appreciate if you'd let them know.

### Wiki

For more information on installation, upgrades, configuration, and integrations please see our [wiki.](https://github.com/plausible/community-edition/wiki)

### Contact

- For release announcements please go to [GitHub releases.](https://github.com/plausible/analytics/releases)
- For a question or advice please go to [GitHub discussions.](https://github.com/plausible/analytics/discussions/categories/self-hosted-support)

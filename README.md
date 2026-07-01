# Link Tracker

Платформа для відстеження переходів за унікальними посиланнями інфлюєнсерів
(`https://mydomain.com/i/{code}`) з миттєвим редиректом на Instagram-магазин,
детальною аналітикою (геолокація, пристрій, бот-фільтрація) та адмін-панеллю.

## Стек технологій

- Java 21, Spring Boot 3.3 (Web, Data JPA, Security, Validation, Thymeleaf)
- PostgreSQL 16 + Flyway (схема в `src/main/resources/db/migration`)
- uap-java — парсинг User-Agent (браузер/ОС/пристрій)
- ip-api.com — геолокація IP (легко замінюється, див. нижче)
- Apache POI (Excel), OpenPDF (PDF) — експорт статистики
- Caffeine — in-memory rate-limiting для виявлення підозрілих переходів
- springdoc-openapi — Swagger UI (`/swagger-ui.html`)
- Bootstrap 5 + Chart.js + Leaflet — адмін-панель
- Docker, docker-compose, Nginx

## Архітектура

```
controller/        REST + Thymeleaf контролери (redirect, admin, API)
service/            бізнес-логіка (interfaces)
service/impl/       реалізації
service/geo/        GeoLocationService — інтерфейс + ip-api.com реалізація
service/export/     CSV / Excel / PDF експорт
entity/             JPA-сутності (Influencer, ClickEvent, AdminUser)
repository/         Spring Data репозиторії з аналітичними запитами
dto/                DTO для API/UI
mapper/              entity <-> DTO
security/            UserDetailsService для адмін-логіну
config/              Security, Async, Swagger, ініціалізація даних
exception/            глобальний обробник помилок
util/                 генератор коротких кодів, визначення IP клієнта, enum DeviceType
```

**Принцип "швидкий редирект"**: `GET /i/{code}` синхронно лише шукає
інфлюєнсера (індексований запит за `code`) і одразу повертає `302 Redirect`.
Збагачення даними (геолокація, парсинг User-Agent, бот-детекція) виконується
асинхронно в окремому пулі потоків (`AsyncConfig#clickTrackingExecutor`) і
ніколи не затримує відповідь користувачу.

## Швидкий старт (Docker)

```bash
git clone <your-repo-url> link-tracker
cd link-tracker
cp .env.example .env
# відредагуйте .env: задайте надійні DB_PASSWORD та ADMIN_PASSWORD

docker compose up -d --build
```

Після старту:

- Адмін-панель: http://localhost:8080/admin (логін/пароль з `.env`, за
  замовчуванням `admin` / `changeme123` — **обов'язково змініть**)
- Swagger UI: http://localhost:8080/swagger-ui.html
- Приклад tracking-посилань (демо-дані з `V2__seed_demo_data.sql`):
  - http://localhost:8080/i/anna
  - http://localhost:8080/i/max
  - http://localhost:8080/i/oleg

Якщо демо-дані не потрібні — видаліть файл
`src/main/resources/db/migration/V2__seed_demo_data.sql` до першого запуску.

## Локальний запуск без Docker

Потрібні: JDK 21, Maven 3.9+, PostgreSQL 16.

```bash
createdb linktracker
export DB_HOST=localhost DB_NAME=linktracker DB_USER=postgres DB_PASSWORD=postgres
mvn spring-boot:run
```

Застосунок підніметься на `http://localhost:8080`, Flyway автоматично
створить схему БД при старті.

## Зміна пароля адміністратора

Пароль адміністратора, заданий через `ADMIN_PASSWORD`, використовується лише
**при першому старті** для створення запису в таблиці `admin_users` (хеш
BCrypt). Щоб змінити пароль пізніше, оновіть хеш напряму в БД, наприклад:

```sql
UPDATE admin_users
SET password_hash = '$2a$10$...'  -- BCrypt-хеш нового пароля
WHERE username = 'admin';
```

(Хеш можна згенерувати будь-яким онлайн/офлайн BCrypt-генератором з cost=10,
або тимчасовим Java/Python-скриптом.)

## Заміна провайдера геолокації

`GeoLocationService` — інтерфейс (`service/geo/GeoLocationService.java`).
Поточна реалізація (`IpApiGeoLocationService`) використовує безкоштовний
ip-api.com. Щоб перейти на MaxMind GeoLite2 (рекомендовано для високого
навантаження, оскільки працює локально без HTTP-запитів):

1. Додайте залежність `com.maxmind.geoip2:geoip2` в `pom.xml`.
2. Створіть `MaxMindGeoLocationService implements GeoLocationService`.
3. Позначте новий бін `@Primary` (або видаліть стару реалізацію) —
   решта коду (контролери, сервіси) не зміниться, оскільки залежить лише
   від інтерфейсу.

## Деплой на VPS

### 1. Підготовка сервера

```bash
sudo apt update && sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker $USER   # перелогіньтесь після цього
```

### 2. Клонування та запуск

```bash
git clone <your-repo-url> /opt/link-tracker
cd /opt/link-tracker
cp .env.example .env
nano .env   # задайте реальні паролі

docker compose up -d --build
```

### 3. Підключення власного домена

1. У DNS-провайдера створіть A-запис: `mydomain.com -> IP_вашого_VPS`
   (і за бажанням `www.mydomain.com -> IP_вашого_VPS`).
2. Відредагуйте `nginx/nginx.conf`, замінивши `mydomain.com` на ваш реальний
   домен (в `server_name` і в закоментованому HTTPS-блоці).
3. Перезапустіть nginx: `docker compose restart nginx`.
4. Перевірте, що `http://mydomain.com` показує застосунок (поки що без HTTPS).

### 4. HTTPS через Let's Encrypt

Найпростіший шлях — окремий короткоживучий контейнер certbot, що працює
разом із вже запущеним nginx (webroot-метод, без зупинки сервісу):

```bash
docker run --rm \
  -v $(pwd)/nginx/certbot-www:/var/www/certbot \
  -v $(pwd)/certbot-conf:/etc/letsencrypt \
  certbot/certbot certonly --webroot \
  -w /var/www/certbot \
  -d mydomain.com -d www.mydomain.com \
  --email you@example.com --agree-tos --no-eff-email
```

(Переконайтесь, що шляхи томів збігаються з тими, що зазначені у
`docker-compose.yml` для сервісу `nginx` — `certbot-www` та `certbot-conf`.)

Після успішного отримання сертифіката:

1. Розкоментуйте HTTPS server-блок у `nginx/nginx.conf` (замінивши
   `mydomain.com` на ваш домен) та замініть верхній HTTP-блок на редирект
   `301 -> https`, як показано в закоментованому прикладі в кінці файлу.
2. `docker compose restart nginx`.
3. Перевірте `https://mydomain.com/admin`.

Для автопродовження сертифіката (дійсний 90 днів) додайте cron-завдання:

```bash
0 3 * * * cd /opt/link-tracker && docker run --rm \
  -v $(pwd)/nginx/certbot-www:/var/www/certbot \
  -v $(pwd)/certbot-conf:/etc/letsencrypt \
  certbot/certbot renew --webroot -w /var/www/certbot && \
  docker compose restart nginx
```

## REST API (короткий огляд)

| Метод | Шлях | Опис |
|---|---|---|
| GET | `/i/{code}` | Публічний редирект + трекінг |
| GET | `/api/influencers` | Список/пошук/пагінація |
| POST | `/api/influencers` | Створення (код автогенерується, якщо не вказано) |
| PUT | `/api/influencers/{id}` | Редагування |
| DELETE | `/api/influencers/{id}` | Видалення |
| PATCH | `/api/influencers/{id}/toggle-active` | Увімкнути/вимкнути |
| POST | `/api/influencers/{id}/regenerate-code` | Новий випадковий код |
| GET | `/api/stats` | Глобальна статистика для дашборду |
| GET | `/api/stats/{id}` | Детальна статистика інфлюєнсера |
| GET | `/api/export/csv` \| `/excel` \| `/pdf` | Експорт (фільтри: `influencerId`, `from`, `to`, `country`, `city`) |

Повна інтерактивна документація — `/swagger-ui.html`. Усі `/api/**` та
`/admin/**` шляхи захищені сесійною автентифікацією (ROLE_ADMIN); `/i/**`
залишається публічним.

## Продуктивність і масштабування

- Редирект не виконує жодного блокуючого I/O окрім одного індексованого
  SELECT за `code`.
- Запис статистики, геолокація та парсинг User-Agent виконуються асинхронно
  на окремому пулі потоків.
- Індекси БД: `influencer_id`, `timestamp`, `ip`, складений
  `(influencer_id, ip, timestamp)`, `country`, `device_type`.
- HikariCP pool, batch-insert для Hibernate.
- При необхідності подальшого масштабування: винесіть запис кліків у
  чергу (Kafka/RabbitMQ) і окремий consumer-сервіс — інтерфейс
  `ClickTrackingService` вже ізолює цю логіку від контролера.

## Що можна розширити далі (закладено в архітектурі через інтерфейси)

- Telegram-бот для перегляду статистики (окремий модуль, що використовує
  `StatsService`).
- QR-коди для кожного посилання (генерація з `Influencer.trackingUrl`).
- Вебхуки / email-сповіщення про нові переходи.
- Інтеграція з Google Analytics / Meta Pixel (додатковий `<script>` у
  redirect-сторінку або server-side event API).
- MaxMind GeoLite2 замість ip-api.com (див. розділ вище).

## Відомі спрощення (через обсяг завдання)

- "Унікальність" переходу визначається за фактом повторного кліку з тієї ж
  пари (influencer, IP) за останні 30 днів — це практичний proxy-показник,
  а не криптографічно точна ідентифікація користувача (що неможливо лише за
  IP/UA без cookies чи fingerprinting).
- Бот-детекція поєднує: (1) сигнатури User-Agent відомих краулерів/ботів,
  (2) rate-limit за парою (influencer, IP) у короткому вікні. Це достатньо
  для типового SMM-кейсу, але не замінює спеціалізовані anti-fraud рішення.
- World-map на дашборді використовує OpenStreetMap/Leaflet (без API-ключа);
  для production з високим навантаженням розгляньте власний тайл-сервер або
  платний провайдер карт.

-- Influencers: one row per influencer, holding their unique tracking code.
CREATE TABLE influencers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    code            VARCHAR(16)     NOT NULL,
    instagram_url   VARCHAR(500)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_influencer_code ON influencers (code);

-- Click events: one row per recorded click/redirect.
CREATE TABLE click_events (
    id                  BIGSERIAL PRIMARY KEY,
    influencer_id       BIGINT          NOT NULL REFERENCES influencers (id) ON DELETE CASCADE,
    timestamp           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    ip                  VARCHAR(64),
    country             VARCHAR(100),
    country_code        VARCHAR(8),
    city                VARCHAR(150),
    region              VARCHAR(150),
    timezone            VARCHAR(100),
    isp                 VARCHAR(255),
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    network_type        VARCHAR(20),
    is_vpn_or_proxy     BOOLEAN,
    is_datacenter       BOOLEAN,
    user_agent          TEXT,
    referer             TEXT,
    device_type         VARCHAR(20),
    browser             VARCHAR(100),
    browser_version     VARCHAR(50),
    os                  VARCHAR(100),
    os_version          VARCHAR(50),
    device_manufacturer VARCHAR(100),
    device_model        VARCHAR(150),
    is_bot              BOOLEAN         NOT NULL DEFAULT FALSE,
    is_unique_visit     BOOLEAN         NOT NULL DEFAULT TRUE,
    is_suspicious       BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_click_influencer ON click_events (influencer_id);
CREATE INDEX idx_click_timestamp ON click_events (timestamp);
CREATE INDEX idx_click_ip ON click_events (ip);
CREATE INDEX idx_click_influencer_ip_timestamp ON click_events (influencer_id, ip, timestamp);
CREATE INDEX idx_click_country ON click_events (country);
CREATE INDEX idx_click_device_type ON click_events (device_type);

-- Admin accounts for the /admin panel and protected API endpoints.
CREATE TABLE admin_users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX idx_admin_username ON admin_users (username);

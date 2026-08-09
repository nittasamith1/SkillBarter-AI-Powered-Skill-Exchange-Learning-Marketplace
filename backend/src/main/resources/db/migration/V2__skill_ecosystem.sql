-- ============================================================
-- V2__skill_ecosystem.sql
-- SkillBarter AI — Phase 2: Skill Ecosystem & Core Marketplace
-- MySQL 8.0+
-- ============================================================

-- ── skill_categories ─────────────────────────────────────────
CREATE TABLE skill_categories (
    id          CHAR(36)        NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    description TEXT,
    parent_id   CHAR(36),
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_skill_categories         PRIMARY KEY (id),
    CONSTRAINT uq_skill_categories_name    UNIQUE (name),
    CONSTRAINT fk_skill_cat_parent         FOREIGN KEY (parent_id) REFERENCES skill_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── skills ────────────────────────────────────────────────────
CREATE TABLE skills (
    id              CHAR(36)        NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    category_id     CHAR(36)        NOT NULL,
    is_global       TINYINT(1)      NOT NULL DEFAULT 1,
    tags            JSON,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_skills                PRIMARY KEY (id),
    CONSTRAINT fk_skills_category       FOREIGN KEY (category_id) REFERENCES skill_categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── skill_prerequisites ───────────────────────────────────────
CREATE TABLE skill_prerequisites (
    skill_id                CHAR(36)    NOT NULL,
    prerequisite_skill_id   CHAR(36)    NOT NULL,

    CONSTRAINT pk_skill_prereqs         PRIMARY KEY (skill_id, prerequisite_skill_id),
    CONSTRAINT fk_prereq_skill          FOREIGN KEY (skill_id)              REFERENCES skills(id) ON DELETE CASCADE,
    CONSTRAINT fk_prereq_prereq         FOREIGN KEY (prerequisite_skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── user_skills ───────────────────────────────────────────────
CREATE TABLE user_skills (
    id                  CHAR(36)    NOT NULL,
    user_id             CHAR(36)    NOT NULL,
    tenant_id           CHAR(36)    NOT NULL,
    skill_id            CHAR(36)    NOT NULL,
    level               ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') NOT NULL DEFAULT 'BEGINNER',
    can_teach           TINYINT(1)  NOT NULL DEFAULT 0,
    want_to_learn       TINYINT(1)  NOT NULL DEFAULT 0,
    years_experience    INT,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_user_skills               PRIMARY KEY (id),
    CONSTRAINT uq_user_skills_user_skill    UNIQUE (user_id, skill_id),
    CONSTRAINT fk_user_skills_user          FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_user_skills_skill         FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── learning_goals ────────────────────────────────────────────
CREATE TABLE learning_goals (
    id                      CHAR(36)    NOT NULL,
    user_id                 CHAR(36)    NOT NULL,
    tenant_id               CHAR(36)    NOT NULL,
    target_skill_id         CHAR(36)    NOT NULL,
    goal_text               TEXT,
    current_level           ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT'),
    target_level            ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT'),
    deadline                DATE,
    learning_preferences    TEXT,
    status                  ENUM('ACTIVE','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_learning_goals            PRIMARY KEY (id),
    CONSTRAINT fk_learning_goals_user       FOREIGN KEY (user_id)         REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_learning_goals_skill      FOREIGN KEY (target_skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── exchange_requests ─────────────────────────────────────────
CREATE TABLE exchange_requests (
    id                  CHAR(36)    NOT NULL,
    tenant_id           CHAR(36)    NOT NULL,
    requester_id        CHAR(36)    NOT NULL,
    receiver_id         CHAR(36)    NOT NULL,
    offered_skill_id    CHAR(36)    NOT NULL,
    wanted_skill_id     CHAR(36)    NOT NULL,
    message             TEXT,
    status              ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_exchange_requests             PRIMARY KEY (id),
    CONSTRAINT fk_exchange_requester            FOREIGN KEY (requester_id)     REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_exchange_receiver             FOREIGN KEY (receiver_id)      REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_exchange_offered_skill        FOREIGN KEY (offered_skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    CONSTRAINT fk_exchange_wanted_skill         FOREIGN KEY (wanted_skill_id)  REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Indexes ───────────────────────────────────────────────────
CREATE INDEX idx_skills_category          ON skills(category_id);
CREATE INDEX idx_skills_is_global         ON skills(is_global);
CREATE INDEX idx_user_skills_user         ON user_skills(user_id);
CREATE INDEX idx_user_skills_tenant       ON user_skills(tenant_id);
CREATE INDEX idx_user_skills_skill        ON user_skills(skill_id);
CREATE INDEX idx_user_skills_can_teach    ON user_skills(can_teach);
CREATE INDEX idx_user_skills_want_learn   ON user_skills(want_to_learn);
CREATE INDEX idx_learning_goals_user      ON learning_goals(user_id);
CREATE INDEX idx_learning_goals_tenant    ON learning_goals(tenant_id);
CREATE INDEX idx_learning_goals_status    ON learning_goals(status);
CREATE INDEX idx_exchange_req_requester   ON exchange_requests(requester_id);
CREATE INDEX idx_exchange_req_receiver    ON exchange_requests(receiver_id);
CREATE INDEX idx_exchange_req_status      ON exchange_requests(status);
CREATE INDEX idx_exchange_req_tenant      ON exchange_requests(tenant_id);

-- ── Seed: Root Categories ─────────────────────────────────────
INSERT INTO skill_categories (id, name, description, parent_id) VALUES
    ('ca000001-0000-0000-0000-000000000001', 'Programming',   'Software development and coding skills', NULL),
    ('ca000001-0000-0000-0000-000000000002', 'Design',        'UI/UX, graphic, and visual design',      NULL),
    ('ca000001-0000-0000-0000-000000000003', 'Data Science',  'Data analysis, ML, and statistics',      NULL),
    ('ca000001-0000-0000-0000-000000000004', 'Business',      'Business, management, and finance',      NULL),
    ('ca000001-0000-0000-0000-000000000005', 'Languages',     'Spoken and written human languages',     NULL),
    ('ca000001-0000-0000-0000-000000000006', 'Mathematics',   'Pure and applied mathematics',           NULL),
    ('ca000001-0000-0000-0000-000000000007', 'Other',         'Miscellaneous skills',                   NULL);

-- ── Seed: Sub-Categories ─────────────────────────────────────
INSERT INTO skill_categories (id, name, description, parent_id) VALUES
    ('ca000002-0000-0000-0000-000000000001', 'Java',           'Java programming language',               'ca000001-0000-0000-0000-000000000001'),
    ('ca000002-0000-0000-0000-000000000002', 'Python',         'Python programming language',             'ca000001-0000-0000-0000-000000000001'),
    ('ca000002-0000-0000-0000-000000000003', 'JavaScript',     'JavaScript and TypeScript',               'ca000001-0000-0000-0000-000000000001'),
    ('ca000002-0000-0000-0000-000000000004', 'System Design',  'Distributed systems and architecture',    'ca000001-0000-0000-0000-000000000001'),
    ('ca000002-0000-0000-0000-000000000005', 'Databases',      'SQL, NoSQL and database design',          'ca000001-0000-0000-0000-000000000001'),
    ('ca000002-0000-0000-0000-000000000006', 'DevOps',         'CI/CD, Docker, Kubernetes',               'ca000001-0000-0000-0000-000000000001'),
    ('ca000002-0000-0000-0000-000000000007', 'UI/UX Design',   'User interface and experience design',    'ca000001-0000-0000-0000-000000000002'),
    ('ca000002-0000-0000-0000-000000000008', 'Machine Learning','ML algorithms and frameworks',           'ca000001-0000-0000-0000-000000000003'),
    ('ca000002-0000-0000-0000-000000000009', 'Data Analysis',  'Excel, Power BI, Tableau',                'ca000001-0000-0000-0000-000000000003');

-- ── Seed: Skills ─────────────────────────────────────────────
INSERT INTO skills (id, name, description, category_id, is_global, tags) VALUES
    -- Java
    ('ab000001-0000-0000-0000-000000000001', 'Java OOP',          'Object-Oriented Programming in Java',         'ca000002-0000-0000-0000-000000000001', 1, '["java","oop","basics"]'),
    ('ab000001-0000-0000-0000-000000000002', 'Java Collections',  'Java Collections Framework',                  'ca000002-0000-0000-0000-000000000001', 1, '["java","collections","data-structures"]'),
    ('ab000001-0000-0000-0000-000000000003', 'Java Multithreading','Concurrency and multithreading in Java',      'ca000002-0000-0000-0000-000000000001', 1, '["java","threads","concurrency"]'),
    ('ab000001-0000-0000-0000-000000000004', 'Spring Boot',       'Spring Boot REST API development',            'ca000002-0000-0000-0000-000000000001', 1, '["java","spring","backend"]'),
    ('ab000001-0000-0000-0000-000000000005', 'Spring Security',   'Authentication and authorization with Spring','ca000002-0000-0000-0000-000000000001', 1, '["java","spring","security"]'),
    -- Python
    ('ab000001-0000-0000-0000-000000000006', 'Python Basics',     'Python fundamentals and syntax',              'ca000002-0000-0000-0000-000000000002', 1, '["python","basics"]'),
    ('ab000001-0000-0000-0000-000000000007', 'Django',            'Web development with Django',                 'ca000002-0000-0000-0000-000000000002', 1, '["python","web","backend"]'),
    ('ab000001-0000-0000-0000-000000000008', 'FastAPI',           'Building APIs with FastAPI',                  'ca000002-0000-0000-0000-000000000002', 1, '["python","api","backend"]'),
    -- JavaScript
    ('ab000001-0000-0000-0000-000000000009', 'JavaScript Basics', 'JavaScript fundamentals and ES6+',           'ca000002-0000-0000-0000-000000000003', 1, '["javascript","basics","frontend"]'),
    ('ab000001-0000-0000-0000-000000000010', 'React',             'React component and state management',        'ca000002-0000-0000-0000-000000000003', 1, '["javascript","react","frontend"]'),
    ('ab000001-0000-0000-0000-000000000011', 'TypeScript',        'TypeScript for type-safe JavaScript',         'ca000002-0000-0000-0000-000000000003', 1, '["javascript","typescript","frontend"]'),
    ('ab000001-0000-0000-0000-000000000012', 'Node.js',           'Server-side JavaScript with Node.js',         'ca000002-0000-0000-0000-000000000003', 1, '["javascript","nodejs","backend"]'),
    -- System Design
    ('ab000001-0000-0000-0000-000000000013', 'System Design Basics','Introduction to distributed systems',       'ca000002-0000-0000-0000-000000000004', 1, '["architecture","design","backend"]'),
    ('ab000001-0000-0000-0000-000000000014', 'Microservices',     'Microservices architecture patterns',         'ca000002-0000-0000-0000-000000000004', 1, '["microservices","architecture"]'),
    -- Databases
    ('ab000001-0000-0000-0000-000000000015', 'SQL',               'Structured Query Language fundamentals',      'ca000002-0000-0000-0000-000000000005', 1, '["sql","databases","basics"]'),
    ('ab000001-0000-0000-0000-000000000016', 'MySQL',             'MySQL database administration and queries',   'ca000002-0000-0000-0000-000000000005', 1, '["sql","mysql","databases"]'),
    ('ab000001-0000-0000-0000-000000000017', 'MongoDB',           'NoSQL document database with MongoDB',        'ca000002-0000-0000-0000-000000000005', 1, '["nosql","mongodb","databases"]'),
    -- DevOps
    ('ab000001-0000-0000-0000-000000000018', 'Docker',            'Containerization with Docker',                'ca000002-0000-0000-0000-000000000006', 1, '["docker","devops","containers"]'),
    ('ab000001-0000-0000-0000-000000000019', 'Git',               'Version control with Git',                    'ca000002-0000-0000-0000-000000000006', 1, '["git","devops","basics"]'),
    -- UI/UX
    ('ab000001-0000-0000-0000-000000000020', 'Figma',             'UI design and prototyping with Figma',        'ca000002-0000-0000-0000-000000000007', 1, '["figma","design","ui"]'),
    ('ab000001-0000-0000-0000-000000000021', 'UX Research',       'User research and usability testing',         'ca000002-0000-0000-0000-000000000007', 1, '["ux","research","design"]'),
    -- Machine Learning
    ('ab000001-0000-0000-0000-000000000022', 'Machine Learning',  'ML algorithms and model training',            'ca000002-0000-0000-0000-000000000008', 1, '["ml","ai","python"]'),
    ('ab000001-0000-0000-0000-000000000023', 'Deep Learning',     'Neural networks and deep learning',           'ca000002-0000-0000-0000-000000000008', 1, '["dl","ai","python"]'),
    -- Data Analysis
    ('ab000001-0000-0000-0000-000000000024', 'Excel',             'Microsoft Excel for data analysis',           'ca000002-0000-0000-0000-000000000009', 1, '["excel","data","analysis"]'),
    ('ab000001-0000-0000-0000-000000000025', 'Power BI',          'Business intelligence with Power BI',         'ca000002-0000-0000-0000-000000000009', 1, '["powerbi","data","analysis"]');

-- ── Seed: Prerequisites ───────────────────────────────────────
INSERT INTO skill_prerequisites (skill_id, prerequisite_skill_id) VALUES
    -- Java Collections requires Java OOP
    ('ab000001-0000-0000-0000-000000000002', 'ab000001-0000-0000-0000-000000000001'),
    -- Java Multithreading requires Java Collections
    ('ab000001-0000-0000-0000-000000000003', 'ab000001-0000-0000-0000-000000000002'),
    -- Spring Boot requires Java OOP
    ('ab000001-0000-0000-0000-000000000004', 'ab000001-0000-0000-0000-000000000001'),
    -- Spring Security requires Spring Boot
    ('ab000001-0000-0000-0000-000000000005', 'ab000001-0000-0000-0000-000000000004'),
    -- React requires JavaScript Basics
    ('ab000001-0000-0000-0000-000000000010', 'ab000001-0000-0000-0000-000000000009'),
    -- TypeScript requires JavaScript Basics
    ('ab000001-0000-0000-0000-000000000011', 'ab000001-0000-0000-0000-000000000009'),
    -- Microservices requires System Design Basics
    ('ab000001-0000-0000-0000-000000000014', 'ab000001-0000-0000-0000-000000000013'),
    -- MySQL requires SQL
    ('ab000001-0000-0000-0000-000000000016', 'ab000001-0000-0000-0000-000000000015'),
    -- Deep Learning requires Machine Learning
    ('ab000001-0000-0000-0000-000000000023', 'ab000001-0000-0000-0000-000000000022');

-- 사용자
CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255),
    name       VARCHAR(100) NOT NULL,
    plan       VARCHAR(20) DEFAULT 'free',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 폼
CREATE TABLE forms (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title            VARCHAR(300) NOT NULL,
    description      TEXT,
    slug             VARCHAR(50) UNIQUE NOT NULL,
    is_active        BOOLEAN DEFAULT true,
    allow_duplicate  BOOLEAN DEFAULT false,
    max_responses    INTEGER,
    closes_at        TIMESTAMP,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- 질문
CREATE TABLE questions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    form_id     UUID NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
    type        VARCHAR(30) NOT NULL,
    title       VARCHAR(500) NOT NULL,
    description TEXT,
    is_required BOOLEAN DEFAULT false,
    position    INTEGER NOT NULL,
    settings    JSONB
);

-- 질문 선택지 (radio, checkbox 유형)
CREATE TABLE question_options (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    label       VARCHAR(200) NOT NULL,
    position    INTEGER NOT NULL
);

-- 응답 (한 사람의 폼 전체 응답)
CREATE TABLE form_responses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    form_id         UUID NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
    response_token  VARCHAR(100) UNIQUE NOT NULL,
    submitted_at    TIMESTAMP DEFAULT NOW()
    -- IP 저장 안 함 (익명성 보장)
);

-- 개별 답변 (질문별 응답 내용)
CREATE TABLE answer_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    response_id  UUID NOT NULL REFERENCES form_responses(id) ON DELETE CASCADE,
    question_id  UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    value        TEXT,
    values       TEXT[],
    number_value DECIMAL
);

-- 인덱스
CREATE INDEX idx_forms_slug        ON forms(slug);
CREATE INDEX idx_forms_user_id     ON forms(user_id);
CREATE INDEX idx_questions_form_id ON questions(form_id);
CREATE INDEX idx_form_responses_form_id ON form_responses(form_id);
CREATE INDEX idx_form_responses_token  ON form_responses(response_token);
CREATE INDEX idx_answer_items_response_id ON answer_items(response_id);
CREATE INDEX idx_answer_items_question_id ON answer_items(question_id);

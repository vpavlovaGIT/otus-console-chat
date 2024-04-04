-- Создание таблицы для хранения вопросов
CREATE TABLE public.questions (
	question_id serial4 NOT NULL,
	question_text text NOT NULL,
	CONSTRAINT questions_pkey PRIMARY KEY (question_id)
);

-- Permissions

ALTER TABLE public.questions OWNER TO postgres;
GRANT ALL ON TABLE public.questions TO postgres;

CREATE TABLE public.answers (
	answer_id serial4 NOT NULL,
	answer_text text NOT NULL,
	question_id int4 NOT NULL,
	is_correct_answer bool NOT NULL,
	CONSTRAINT answers_pkey PRIMARY KEY (answer_id),
	CONSTRAINT answers_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(question_id)
);

-- Permissions

ALTER TABLE public.answers OWNER TO postgres;
GRANT ALL ON TABLE public.answers TO postgres;

-- Вставка примера данных в таблицу Questions
INSERT INTO Questions (question_text) VALUES
('Какое животное изображено на рисунке?'),
('Сколько лап у льва?'),
('На какую скорость способен развиться гепард?');

-- Вставка примера данных в таблицу Answers
INSERT INTO Answers (answer_text, question_id, is_correct_answer) VALUES
('Слон', 1, FALSE),
('Лев', 1, FALSE),
('Зебра', 1, TRUE),
('4', 2, FALSE),
('6', 2, TRUE),
('8', 2, FALSE),
('100 км/ч', 3, FALSE),
('80 км/ч', 3, TRUE),
('120 км/ч', 3, FALSE);
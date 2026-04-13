-- Insert initial muscle groups
INSERT INTO muscle_groups (name, description) VALUES
('Pecho', 'Músculos del pecho incluyendo pectorales mayores y menores'),
('Espalda', 'Músculos de la espalda incluyendo dorsales, trapecios y romboides'),
('Hombros', 'Músculos deltoides y músculos rotadores del hombro'),
('Bíceps', 'Músculos flexores del brazo'),
('Tríceps', 'Músculos extensores del brazo'),
('Antebrazos', 'Músculos del antebrazo y muñeca'),
('Core', 'Abdominales, oblicuos y músculos estabilizadores'),
('Cuádriceps', 'Músculos anteriores del muslo'),
('Isquiotibiales', 'Músculos posteriores del muslo'),
('Glúteos', 'Músculos glúteos mayor, medio y menor'),
('Pantorrillas', 'Músculos de la pantorrilla'),
('Piernas completas', 'Ejercicios que involucran múltiples músculos de las piernas')
ON CONFLICT (name) DO NOTHING;

-- Insert initial exercises for chest
INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Flexiones',
    'Ejercicio básico de peso corporal para el pecho',
    id,
    'Peso corporal',
    'Principiante',
    'Acuéstate boca abajo, coloca las manos en el suelo a la altura de los hombros, baja el cuerpo hasta que el pecho casi toque el suelo, luego empuja hacia arriba.'
FROM muscle_groups WHERE name = 'Pecho'
ON CONFLICT DO NOTHING;

INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Press de banca',
    'Ejercicio de fuerza con barra',
    id,
    'Banca y barra',
    'Intermedio',
    'Acuéstate en la banca, agarra la barra a la altura de los hombros, baja hacia el pecho y presiona hacia arriba.'
FROM muscle_groups WHERE name = 'Pecho'
ON CONFLICT DO NOTHING;

INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Aperturas con mancuernas',
    'Movimiento de aislamiento para pecho',
    id,
    'Mancuernas y banca',
    'Intermedio',
    'Acuéstate en la banca con mancuernas, baja los brazos en forma de arco hasta la altura del pecho y vuelve a la posición inicial.'
FROM muscle_groups WHERE name = 'Pecho'
ON CONFLICT DO NOTHING;

-- Insert initial exercises for back
INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Dominadas',
    'Ejercicio de tracción con peso corporal',
    id,
    'Barra de dominadas',
    'Intermedio',
    'Cuelga de la barra, tira hacia arriba hasta que tu barbilla supere la barra, luego baja de forma controlada.'
FROM muscle_groups WHERE name = 'Espalda'
ON CONFLICT DO NOTHING;

INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Remo con barra',
    'Ejercicio de tracción horizontal',
    id,
    'Barra y banco',
    'Intermedio',
    'Inclínate 45 grados, agarra la barra a la altura de la cadera, tira hacia el abdomen y baja de forma controlada.'
FROM muscle_groups WHERE name = 'Espalda'
ON CONFLICT DO NOTHING;

-- Insert initial exercises for legs
INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Sentadillas',
    'Ejercicio fundamental para piernas',
    id,
    'Barra o peso corporal',
    'Principiante',
    'De pie con los pies al ancho de los hombros, baja las caderas hacia atrás y hacia abajo, mantén el pecho levantado y vuelve a subir.'
FROM muscle_groups WHERE name = 'Cuádriceps'
ON CONFLICT DO NOTHING;

INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Peso muerto',
    'Ejercicio de potencia completo',
    id,
    'Barra y discos',
    'Avanzado',
    'De pie ante la barra, agachate manteniendo la espalda recta, tira de la barra hacia arriba hasta estar de pie completamente.'
FROM muscle_groups WHERE name = 'Piernas completas'
ON CONFLICT DO NOTHING;

INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Extensión de pierna',
    'Movimiento de aislamiento para cuádriceps',
    id,
    'Máquina de extensión',
    'Principiante',
    'Siéntate en la máquina, extiende las piernas hacia adelante contra la resistencia y baja de forma controlada.'
FROM muscle_groups WHERE name = 'Cuádriceps'
ON CONFLICT DO NOTHING;

-- Insert initial exercises for core
INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Abdominales básicos',
    'Ejercicio para el recto abdominal',
    id,
    'Peso corporal o colchoneta',
    'Principiante',
    'Acuéstate boca arriba con las rodillas flexionadas, contrae el abdomen y levanta el torso hacia las rodillas.'
FROM muscle_groups WHERE name = 'Core'
ON CONFLICT DO NOTHING;

INSERT INTO exercises (name, description, muscle_group_id, equipment_required, difficulty_level, instructions)
SELECT 
    'Plancha',
    'Ejercicio isométrico para el core',
    id,
    'Peso corporal',
    'Principiante',
    'En posición de flexión, mantén el cuerpo en línea recta desde la cabeza hasta los talones durante el tiempo establecido.'
FROM muscle_groups WHERE name = 'Core'
ON CONFLICT DO NOTHING;
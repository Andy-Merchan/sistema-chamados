-- A V2 já foi aplicada, então não pode ser editada (o Flyway acusaria checksum diferente).
-- Os usuários iniciais são corrigidos aqui, numa nova versão, localizados pelo email original.

UPDATE usuario
SET nome = 'Andy Merchan', email = 'andy.merchan@escolar.ifrn.edu.br', matricula = '20261041010001'
WHERE email = 'maria.oliveira@escolar.ifrn.edu.br';

UPDATE usuario
SET nome = 'Erick Orrala', email = 'erick.orrala@escolar.ifrn.edu.br', matricula = '20261041010002'
WHERE email = 'joao.souza@ifrn.edu.br';

UPDATE usuario
SET nome = 'David Parraga', email = 'david.parraga@ifrn.edu.br', matricula = '2345678'
WHERE email = 'carlos.lima@ifrn.edu.br';

UPDATE usuario
SET nome = 'Pamela Merchan', email = 'pamela.merchan@ifrn.edu.br', matricula = '3456789'
WHERE email = 'ana.costa@ifrn.edu.br';

-- Mais usuários para testar os filtros (nome, tipo, setorId): dois solicitantes e um técnico por setor
INSERT INTO usuario (nome, email, matricula, tipo, setor_id) VALUES
    ('Lucía Benítez', 'lucia.benitez@escolar.ifrn.edu.br', '20261041010003', 'SOLICITANTE', NULL),
    ('Mateo Zambrano', 'mateo.zambrano@ifrn.edu.br', '4567890', 'SOLICITANTE', NULL),
    ('Valeria Cedeño', 'valeria.cedeno@ifrn.edu.br', '5678901', 'TECNICO',
        (SELECT id FROM setor WHERE sigla = 'COTIC')),
    ('Joaquín Mendoza', 'joaquin.mendoza@ifrn.edu.br', '6789012', 'TECNICO',
        (SELECT id FROM setor WHERE sigla = 'MANUT'));

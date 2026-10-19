INSERT INTO setor (nome, sigla) VALUES
    ('Coordenação de Tecnologia da Informação', 'COTIC'),
    ('Manutenção e Infraestrutura', 'MANUT');

INSERT INTO categoria (nome, descricao, setor_id) VALUES
    ('Rede e Internet', 'Problemas de Wi-Fi, cabeamento e acesso à internet',
        (SELECT id FROM setor WHERE sigla = 'COTIC')),
    ('Equipamentos de Informática', 'Computadores, projetores, impressoras e periféricos',
        (SELECT id FROM setor WHERE sigla = 'COTIC')),
    ('Elétrica', 'Tomadas, iluminação e quadros de energia',
        (SELECT id FROM setor WHERE sigla = 'MANUT')),
    ('Climatização', 'Ar-condicionado e ventiladores',
        (SELECT id FROM setor WHERE sigla = 'MANUT'));

-- Solicitantes não pertencem a setor (setor_id nulo)
INSERT INTO usuario (nome, email, matricula, tipo, setor_id) VALUES
    ('Maria Oliveira', 'maria.oliveira@escolar.ifrn.edu.br', '20261041010001', 'SOLICITANTE', NULL),
    ('João Souza', 'joao.souza@ifrn.edu.br', '1234567', 'SOLICITANTE', NULL),
    ('Carlos Lima', 'carlos.lima@ifrn.edu.br', '2345678', 'TECNICO',
        (SELECT id FROM setor WHERE sigla = 'COTIC')),
    ('Ana Costa', 'ana.costa@ifrn.edu.br', '3456789', 'TECNICO',
        (SELECT id FROM setor WHERE sigla = 'MANUT'));

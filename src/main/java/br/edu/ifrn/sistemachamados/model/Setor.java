package br.edu.ifrn.sistemachamados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "setor")
@Getter
@Setter
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String sigla;

    // Lado inverso: a chave estrangeira fica em categoria.setor_id
    @OneToMany(mappedBy = "setor")
    private List<Categoria> categorias = new ArrayList<>();

    // Apenas técnicos têm setor, então esta lista contém os técnicos do setor
    @OneToMany(mappedBy = "setor")
    private List<Usuario> tecnicos = new ArrayList<>();
}

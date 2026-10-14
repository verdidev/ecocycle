package br.com.fiap.ecocycle.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_MATERIAL")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // Coluna NUMBER(10,2) na migration V1. O columnDefinition permite que o
    // ddl-auto=validate do Hibernate aceite o mapeamento Double -> NUMBER no Oracle.
    @Column(name = "peso", columnDefinition = "NUMBER(10,2)")
    private Double peso;

    private String tipo;

    public Material() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
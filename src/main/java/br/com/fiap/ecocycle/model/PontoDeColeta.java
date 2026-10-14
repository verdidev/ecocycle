package br.com.fiap.ecocycle.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_PONTO_COLETA")
public class PontoDeColeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String endereco;

    // Antes era @Transient e o valor enviado na API era descartado.
    // Agora e persistido na coluna tipo_lixo criada pela migration V1.
    @Column(name = "tipo_lixo")
    private String tipoResiduo;

    public PontoDeColeta() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getTipoResiduo() { return tipoResiduo; }
    public void setTipoResiduo(String tipoResiduo) { this.tipoResiduo = tipoResiduo; }
}
package br.com.fiap.ecocycle.repository;

import br.com.fiap.ecocycle.model.PontoDeColeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PontoDeColetaRepository extends JpaRepository<PontoDeColeta, Long> {
}

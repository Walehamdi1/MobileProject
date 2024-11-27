package com.work.truetech.services;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.entity.Facture;
import com.work.truetech.entity.Status;

import java.util.List;

public interface IFactureService {
    Facture createFacture(FactureDTO factureDTO);
    List<Facture> retrieveAllFacture();
    void cancelFacture(Long factureId);
    double calculateTotalSumOfAllFactures();
    Facture toggleFactureStatus(Long factureId);
    Facture updateFactureStatus(Long factureId, Status newStatus);
    void deleteFacture();
    List<Facture> getFacturesByUserId(Long userId);
}

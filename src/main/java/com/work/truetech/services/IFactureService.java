package com.work.truetech.services;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.entity.Facture;

public interface IFactureService {
    Facture createFacture(FactureDTO factureDTO);
}

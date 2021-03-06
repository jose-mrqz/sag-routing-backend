package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.TruckModel;
import pe.sag.routing.data.parser.TruckModelParser;
import pe.sag.routing.data.repository.TruckModelRepository;
import pe.sag.routing.shared.dto.TruckModelDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TruckModelService {
    public TruckModel Truck;
    private final TruckModelRepository truckModelRepository;

    public TruckModelService(TruckModelRepository truckModelRepository) {
        this.truckModelRepository = truckModelRepository;
    }

    public TruckModel register(TruckModelDto truckModelRequest) {
        TruckModel truckModel = TruckModelParser.fromDto(truckModelRequest);
        return truckModelRepository.save(truckModel);
    }

    public List<TruckModelDto> list() {
        return truckModelRepository.findAll().stream().map(TruckModelParser::toDto).collect(Collectors.toList());
    }

    public TruckModel getByCode(String code) {
        Optional<TruckModel> truckModelOptional = truckModelRepository.findTopByCodeContaining(code);
        return truckModelOptional.orElse(null);
    }
}

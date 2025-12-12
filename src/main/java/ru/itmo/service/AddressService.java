package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.*;
import ru.itmo.model.Address;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import ru.itmo.repository.AddressRepository;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class AddressService {
    @Inject
    private AddressRepository addressRepository;

    public List<Address> findAllAddressesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        return addressRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
    }

    public Address findAddressById(Long id) {
        return addressRepository.findById(id);
    }

    public Address createAddress(Address address) {
        return addressRepository.create(address);
    }

    public Address updateAddress(Long id, Address address) {
        return addressRepository.update(id, address);
    }

    public void deleteAddress(Long id) {
        Address address = addressRepository.findById(id);
        if (address == null) throw new NoResultException("Address not found");
        addressRepository.delete(address);
    }

    public List<Address> findAllAddressesTruncated() {
        return addressRepository.findAllTruncated();
    }
}

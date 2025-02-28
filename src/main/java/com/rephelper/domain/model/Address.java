package com.rephelper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object que representa um endereço.
 * Value Objects são imutáveis e identificados por seus atributos.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Address {
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;

    /**
     * Retorna uma representação textual completa do endereço
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();

        sb.append(street).append(", ").append(number);

        if (complement != null && !complement.isBlank()) {
            sb.append(" - ").append(complement);
        }

        sb.append(", ").append(neighborhood)
                .append(", ").append(city)
                .append(" - ").append(state)
                .append(", ").append(zipCode);

        return sb.toString();
    }

    /**
     * Cria uma cópia atualizada do endereço
     */
    public Address withUpdates(
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state,
            String zipCode) {

        return Address.builder()
                .street(street != null ? street : this.street)
                .number(number != null ? number : this.number)
                .complement(complement != null ? complement : this.complement)
                .neighborhood(neighborhood != null ? neighborhood : this.neighborhood)
                .city(city != null ? city : this.city)
                .state(state != null ? state : this.state)
                .zipCode(zipCode != null ? zipCode : this.zipCode)
                .build();
    }
}
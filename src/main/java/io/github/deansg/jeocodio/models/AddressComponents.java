package io.github.deansg.jeocodio.models;

/**
 * See updated, full documentations <a href="https://www.geocod.io/docs/#address-components">here</a>
 *
 * @param number          House number, e.g. "2100" or "250 1/2"
 * @param predirectional  Directional that comes before the street name, 1-2 characters, e.g. N or NE
 * @param prefix          Abbreviated street prefix, particularly common in the case of French address e.g. Rue, Boulevard, Impasse
 * @param street          Name of the street without number, prefix or suffix, e.g. "Main"
 * @param suffix          Abbreviated street suffix, e.g. St., Ave. Rd.
 * @param postdirectional Directional that comes after the street name, 1-2 characters, e.g. N or NE
 * @param secondaryunit   Name of the secondary unit, e.g. "Apt" or "Unit". For "input" address components only
 * @param secondarynumber Secondary unit number. For "input" address components only
 * @param city
 * @param county
 * @param state
 * @param zip             5-digit zip code for US addresses. The 3-character FSA is returned for Canadian results - the full postal code is not returned
 * @param country
 * @param formattedStreet Fully formatted street, including all directionals, suffix/prefix but not house number
 */
public record AddressComponents(String number,
                                String predirectional,
                                String prefix,
                                String street,
                                String suffix,
                                String postdirectional,
                                String secondaryunit,
                                String secondarynumber,
                                String city,
                                String county,
                                String state,
                                String zip,
                                String country,
                                String formattedStreet) {
}

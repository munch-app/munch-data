package munch.data;

import catalyst.license.LicenseValueSanitizer;
import catalyst.license.ValuePermission;
import catalyst.mutation.PlaceMutation;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 4/10/18
 * Time: 10:36 AM
 * Project: catalyst
 */
@Singleton
public class PlaceDataLicensing {
    private final LicenseValueSanitizer sanitizer;

    @Inject
    public PlaceDataLicensing(LicenseValueSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    /**
     * Cleaned mutation might fail ValidationException.validate because required value is removed
     * Must be used by a plugins to ensure plugin consumer is not conflicting on data rules
     * Catalyst system will not be affected as it is internal
     *
     * @param mutation to sanitize & normalise to for use
     */
    public void sanitize(PlaceMutation mutation) {
        sanitizer.sanitize(ValuePermission.Name, mutation.getName());

// Tag is not sanitized because the parser will tag care of it
//        sanitizer.sanitize(mutation.getTag());

//        sanitizer.sanitize(mutation.getStatus());
        sanitizer.sanitize(mutation.getPhone());
        sanitizer.sanitize(mutation.getWebsite());
        sanitizer.sanitize(mutation.getDescription());

        sanitizer.sanitize(mutation.getAddress());
        sanitizer.sanitize(mutation.getStreet());
        sanitizer.sanitize(mutation.getUnitNumber());
//        sanitizer.sanitize(mutation.getPostcode());

        sanitizer.sanitize(mutation.getMenuPricePerPax());
        sanitizer.sanitize(mutation.getMenuUrl());

        sanitizer.sanitize(mutation.getHour());

        // No need to remove because it is resolved via Nominatim
//        sanitizer.sanitize(mutation.getLatLng());
    }
}

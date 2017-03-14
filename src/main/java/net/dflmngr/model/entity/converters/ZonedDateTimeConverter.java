package net.dflmngr.model.entity.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import net.dflmngr.utils.DflmngrUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Date>{
    @Override
    public Date convertToDatabaseColumn(ZonedDateTime date) {
        Instant instant = Instant.from(date);
        return Date.from(instant);
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Date value) {
        Instant instant = value.toInstant();
        //LocalDateTime localTime = LocalDateTime.from(instant);
        return ZonedDateTime.ofInstant(instant, ZoneId.of(DflmngrUtils.defaultTimezone));
        //return localTime.atZone(ZoneId.of("UTC"));
    }
}

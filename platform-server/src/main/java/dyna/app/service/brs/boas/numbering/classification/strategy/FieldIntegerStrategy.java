package dyna.app.service.brs.boas.numbering.classification.strategy;

import java.math.BigDecimal;

import dyna.app.service.brs.boas.numbering.classification.AllocateParameter;
import dyna.app.service.brs.boas.numbering.classification.FieldStratery;
import dyna.common.exception.ServiceRequestException;
import org.springframework.stereotype.Component;

@Component
public class FieldIntegerStrategy extends FieldStratery
{

	@Override
	protected String getSourceDate(AllocateParameter parameter) throws ServiceRequestException
	{
		Object value = this.getValue(parameter.classificationMap, parameter.field, parameter.item,
				parameter.field.getFieldName());
		if (value == null)
		{
			return null;
		}
		if (value instanceof BigDecimal)
		{
			return ((BigDecimal) value).intValue() + "";
		}
		else if (value instanceof Integer)
		{
			return value.toString();
		}

		return value.toString();
	}

}

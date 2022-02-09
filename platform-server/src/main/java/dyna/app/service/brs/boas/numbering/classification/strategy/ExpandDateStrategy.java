package dyna.app.service.brs.boas.numbering.classification.strategy;

import dyna.app.service.brs.boas.numbering.classification.DateAndTimeStrategy;
import dyna.common.util.DateFormat;
import org.springframework.stereotype.Component;

@Component
public class ExpandDateStrategy extends DateAndTimeStrategy
{
	@Override
	protected String getDefaultFormat()
	{
		return DateFormat.PTN_YMDHMS;
	}

}

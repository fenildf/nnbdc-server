package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Event;

@Service("EventBO")
@Scope("prototype")
public class EventBO extends BaseBo<Event> {
	public EventBO() {
		setDao(new BaseDao<Event>() {
		});
	}
}
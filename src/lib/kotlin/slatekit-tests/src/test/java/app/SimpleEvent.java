package app;

import meta.ModelHelpers;
import slatekit.entities.Entity;
import slatekit.meta.models.Model;

// Temp to represent a vacation day
// e.g. date of vacation, total days, and reason
public class SimpleEvent extends AppEvent implements Entity<Integer> {

    public SimpleEvent() {
        super("simple-event");
    }


    @Override
    public Model toModel() {
        return asModel();
    }


    public static Model asModel() {

        Model model = new Model(ModelHelpers.model(SimpleEvent.class), "");
        Model finalModel = AppSchema.setupEventMappings(model);
        return finalModel;
    }
}

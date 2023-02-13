package org.SyCache.CacheEntity;

import jakarta.persistence.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

@Entity
@Table(name = "Stream")
public class CacheInlineFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;
    String name;
    String function;

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public Value parse(){
        Context context = Context.newBuilder()
                .allowAllAccess(true)
                .build();

        Value func = context.eval("js", function);

        assert func.canExecute();

        return func;
    }

}

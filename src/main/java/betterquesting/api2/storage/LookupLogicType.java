package betterquesting.api2.storage;

import java.util.function.Function;
import java.util.function.Predicate;

import static betterquesting.api2.storage.SimpleDatabase.CACHE_MAX_SIZE;
import static betterquesting.api2.storage.SimpleDatabase.SPARSE_RATIO;

enum LookupLogicType {

    Empty(db -> db.mapDB.isEmpty(), EmptyLookupLogic::new),
    ArrayCache(db -> db.mapDB.size() < CACHE_MAX_SIZE && db.mapDB.size() > SPARSE_RATIO * (db.mapDB.lastKey() - db.mapDB.firstKey()),
               ArrayCacheLookupLogic::new),
    Naive(db -> true, NaiveLookupLogic::new);
    private final Predicate<AbstractDatabase<?>> shouldUse;
    private final Function<AbstractDatabase<?>, LookupLogic<?>> factory;

    LookupLogicType(Predicate<AbstractDatabase<?>> shouldUse, Function<AbstractDatabase<?>, LookupLogic<?>> factory) {
        this.shouldUse = shouldUse;
        this.factory = factory;
    }

    static LookupLogicType determine(AbstractDatabase<?> db) {
        for (LookupLogicType type : values()) {
            if (type.shouldUse.test(db))
                return type;
        }
        return Naive;
    }

    @SuppressWarnings("unchecked")
    <T> LookupLogic<T> get(AbstractDatabase<T> db) {
        return (LookupLogic<T>) factory.apply(db);
    }

}

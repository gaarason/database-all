package gaarason.database.eloquent.repository;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.exception.InvalidPrimaryKeyTypeException;
import gaarason.database.util.EntityUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

abstract class BaseInitializing<T, K> implements Model<T, K> {


}

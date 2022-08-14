import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
<#if swagger_import??>
    ${swagger_import}
</#if>
import lombok.Data;
<#if validator_import??>
    ${validator_import}
</#if>
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

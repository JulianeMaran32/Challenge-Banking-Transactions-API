package juhmaran.challenge.bankingtransactionsapi.infrastructure.mapper;

import juhmaran.challenge.bankingtransactionsapi.domain.entity.Account;
import juhmaran.challenge.bankingtransactionsapi.infrastructure.dto.response.AccountBalanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

  @Mapping(target = "accountNumber", source = "accountNumber")
  @Mapping(target = "balance", source = "balance")
  AccountBalanceResponse toResponse(Account account);

}

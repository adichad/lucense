package com.cleartrip.sw.search.searchj;

import javax.servlet.http.HttpServletRequest;

public abstract class CommandMapper {
  public abstract <T extends Command> T mapCommand(HttpServletRequest request);
  
}

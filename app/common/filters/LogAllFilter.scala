package common.filters

import javax.inject.Inject
import common.filters.AccessLogAndTimerFilter.AccessLogAndTimerFilter
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter

class LogAllFilter @Inject()(
                              gzip: GzipFilter,
                              log: AccessLogAndTimerFilter

                          ) extends DefaultHttpFilters(gzip, log) { }

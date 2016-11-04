typedef struct {
    b32_s id;
    b8 module;
} status_t;
typedef b32_s status_i_t;
#define status_init status_t status={0,0}
#define status_ii_init b32_t status
#define status_io_goto(status_module,status_id) status.module=status_module;status.id=status_id;goto exit
#define status_io_return(module,id) return(((status_t)({module,module,id,id})))
#define status_require_check(status,cont) if(!status_success_p(status)){cont;}
#define status_require_check_goto(status) status_require_check(status,goto exit)
#define status_require_check_return(status) status_require_check(status,return(status))
#define status_require(expression,cont) status=expression;status_require_check(status,cont)
#define status_require_goto(expression) status_require(expression,goto exit)
#define status_require_return(expression) status_require(expression,return(status))
#define status_io_require_check(status_module,status,cont) if(!status_success_p(status)){status.module=status_module;cont;}
#define status_io_require_check_goto(module,status) status_io_require_check(module,status,goto exit)
#define status_io_require_check_return(module,status) status_io_require_check(module,status,return(status))
#define status_io_require(module,expression,cont) status.id=expression;status_io_require_check(module,status,cont)
#define status_io_require_goto(module,expression) status_io_require(module,expression,goto exit)
#define status_io_require_return(module,expression) status_io_require(module,expression,return(status))
#define status_ii_require_check(status,cont) if(!status_success_p(status)){cont;}
#define status_ii_require(expression,cont) status=expression;status_ii_require_check(status,cont)
#define status_ii_require_goto(expression) status_ii_require(expression,goto exit)
#define status_ii_require_return(expression) status_ii_require(expression,return(status))
#define status_success 0
#define status_success_p(a) (status_success==a.id)
#define status_failure_p(a) !status_success_p(a)
#define status_from_boolean(a) !a

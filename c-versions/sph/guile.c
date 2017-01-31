
#define scm_first SCM_CAR
#define scm_tail SCM_CDR
#define scm_c_define_procedure_c_init SCM scm_c_define_procedure_c_temp
#define scm_c_define_procedure_c(name,required,optional,rest,c_function,documentation) scm_c_define_procedure_c_temp=scm_c_define_gsubr(name,required,optional,rest,c_function);\
  scm_set_procedure_property_x(scm_c_define_procedure_c_temp,scm_from_locale_symbol("documentation"),scm_from_locale_string(documentation))
#define scm_c_list_each(list,e,body) while(!scm_is_null(list)){e=scm_first(list);body;list=scm_tail(list);}
#define scm_is_undefined(a) (SCM_UNDEFINED==a)
#define scm_false_if_undefined(a) (scm_is_undefined(a)?SCM_BOOL_F:a)
#define null_if_undefined(a) (scm_is_undefined(a)?0:a)
#define scm_if_undefined_expr(a,b,c) (scm_is_undefined(a)?b:c)
#define scm_if_undefined(a,b,c) if(scm_is_undefined(a)){b;}else{c;}
#define scm_is_list_false_or_undefined(a) (scm_is_true(scm_list_p(a))||(SCM_BOOL_F==a)||(SCM_UNDEFINED==a))
#define scm_is_integer_false_or_undefined(a) (scm_is_integer(a)||(SCM_BOOL_F==a)||(SCM_UNDEFINED==a))
SCM scm_c_bytevector_take(size_t size_octets, b8* a)
{
    SCM r = scm_c_make_bytevector(size_octets);
    memcpy(SCM_BYTEVECTOR_CONTENTS(r), a, size_octets);
    return(r);
};
b0 scm_debug_log(SCM value)
{
    scm_call_2(scm_variable_ref(scm_c_lookup("write")), value, scm_current_output_port());
    scm_newline(scm_current_output_port());
};
#define scm_c_local_define_malloc(variable_name,type) type* variable_name=malloc(sizeof(type));\
  if(!variable_name){scm_c_local_error("memory",0);}
#define scm_c_local_define_malloc_and_size(variable_name,type,size) type* variable_name=malloc(size);\
  if(!variable_name){scm_c_local_error("memory",0);}
#define scm_c_local_error_return return(scm_c_local_error_create)
#if local_error_assert_enable

#define scm_c_local_error_assert(name,expr) if(!expr){scm_c_local_error(name,0);}

#else

#define scm_c_local_error_assert(name,expr) null

#endif
#define scm_c_local_error_glibc(error_number) scm_c_local_error("glibc",scm_from_locale_string(strerror(error_number)))
#define scm_c_local_error_system scm_c_local_error("system",scm_from_locale_string(strerror(errno)))
#define scm_c_require_success_glibc(a) s=a;\
  if((s<0)){scm_c_local_error_glibc(s);}
#define scm_c_require_success_system(a) if((a<0)){scm_c_local_error_system;}
enum {sph_guile_status_id_wrong_argument_type};
#define sph_guile_status_group_sph_guile 4
b8* sph_guile_status_text(status_t status)
{
    return(((sph_guile_status_group_sph_guile == status.group) ? string_append("sph-guile: ", ((b8*)(((sph_guile_status_id_wrong_argument_type == status.id) ? "wrong type for argument" : "error without description")))) : ((b8*)(""))));
};
#if scm_enable_typechecks_p

#define scm_typecheck(expr) if(!expr){status.id=sph_guile_status_id_wrong_argument_type;status.group=sph_guile_status_group_sph_guile;goto exit;}

#else

#define scm_typecheck(expr) null

#endif

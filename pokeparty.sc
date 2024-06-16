__config() -> 
{
  'scope' -> 'global',
  'command_permission' -> 'all',
  'commands' -> 
  {
    'timer report' -> 'report_timer',
    'subscribe' -> 'subscribe',
    'subscribe list' -> 'print_subscriber_list',
    'unsubscribe' -> 'unsubscribe',
  },
};

global_config = {};
global_subscribers = [];
global_timestamp = 0;

pokeparty() ->
(
  run('say It\'s PokeParty Time!');
  run('say 구독하기 \/pokeparty subscribe');

  for(global_subscribers, 
    give_random_pokemon(_);
  );
);

give_random_pokemon(player) ->
(
  level = get_random_level();
  shiny = if(is_shiny(), 'shiny', '');
  command = str('givepokemonother %s random level=%d %s', player, level, shiny);
  [success_count, output_message, error_message] = 
      run(command);

  is_success_run = success_count != 0;
  if(is_success_run,
    output_message_string = join('\n', output_message);
    print(player, output_message_string);
  , 
    print(player, error_message);
  );
);

is_shiny() ->
(
  config_shiny = global_config:'shiny';
  shiny_probability = config_shiny:'probability';
  if(bool(!rand(shiny_probability^(-1))),
    return(true);
  );
  return(false);
);

get_random_level() ->
(
  config_level = global_config:'level';
  level_base = config_level:'base';
  level_variance = config_level:'variance';
  
  level = level_base 
      + (-1)^floor(rand(2)) * floor(rand(level_variance));
  return(level);
);

reset_timer() ->
(
  global_timestamp = tick_time();
);

report_timer() ->
(
  current_timestamp = tick_time();
  time_elapsed = current_timestamp - global_timestamp;
  ticks_left = get_countdown() - time_elapsed;
  print(format(' Ticks before the next PokeParty: ', str('d %d', ticks_left)));
);

subscribe() -> 
(
  if(is_player_subscriber(),
    print('You are already a subscriber');
    return();
  );

  player_name = query(player(), 'name');
  global_subscribers += player_name;

  write_file('pokeparty-subscribers', 'shared_json', global_subscribers);
  print('Successfully subscribed to PokeParty');
);

unsubscribe() ->
(
  if(!is_player_subscriber(),
    print('You are not a subscriber');
    return();
  );

  player_name = query(player(), 'name');
  global_subscribers = filter(global_subscribers, _ != player_name);

  write_file('pokeparty-subscribers', 'shared_json', global_subscribers);
  print('Successfully unsubscribed to PokeParty');
);

is_player_subscriber() ->
(
  player_name = query(player(), 'name');
  return(length(filter(global_subscribers, _ == player_name)) != 0);
);

print_subscriber_list() ->
(
  for(global_subscribers,
    print(format('y' + ' ' + _));
  );
);

get_countdown() ->
(
  config_timer = global_config:'timer';
  return(floor(config_timer:'countdown' * config_timer:'multiplier'));
);

__on_start() ->
(
  global_config = read_file('config', 'json');
  global_subscribers = read_file('pokeparty-subscribers', 'shared_json');
  reset_timer();
);

__on_tick() ->
(
  current_timestamp = tick_time();
  time_elapsed = current_timestamp - global_timestamp;
  if(time_elapsed < 0,
    global_timestamp = current_timestamp;
  );

  is_time_pokeparty = 
      time_elapsed > get_countdown();

  if(is_time_pokeparty,
    pokeparty();
    global_timestamp = current_timestamp;
  );
);

require 'yaml'
require 'json'
require 'pp'

require ARGV[0]

def file_as_array(filename)
  data = []
  begin
    f = File.open(filename, "r")
    f.each_line do |line|
      line = line.chomp
      line = line.strip
      data << line
    end
  rescue Exception => e
    @logger.error(e)
    puts e
  rescue
    f.close()
  end

  return data
end

cond_map = {}
file_as_array(ARGV[1]).each { |line|
  curr_vals = line.split(",")
  rank = curr_vals[0]
  curr_vals = curr_vals[1 .. curr_vals.length-1]
  curr_map = cond_map
  i=0
  curr_vals.each { |val|
    val = @@vars[i]["vals"][@@vars[i]["keys"].index(val)]
    if (!curr_map.include?(val))
      if (i<(curr_vals.length-1))
        curr_map[val] = {}
      else
        curr_map[val] = rank.to_i;
      end
    end
#    curr_map.sort_by {|k,v| @@vars[i]["keys"].index(k)}
    i+=1
    curr_map = curr_map[val]
  }
}

cond_map.sort_by {|k,v| k}
#pp cond_map

def print_expression(map, level)
  s=""
  if(map.kind_of?(Hash))
    i=0
    n=0
    map.keys.sort_by{|v| @@vars[level]["vals"].index(v)}.each { |cond|
      if (n<map.keys.size-1)
        s+="\n"
        x=0
        while (x<level+i)
          s+=" "
          x+=1
        end
        if(level>0||n>0)
          s+=","
        end
        s += "if("+cond
        i+=1
      end
      s+=print_expression(map[cond], level+1)
      n+=1
    }
    x=0
    while(x<i) 
      s+="\n"
      y=0
      while (y<level+i-x-1)
        s+=" "
        y+=1
      end
      s+=")"
      x+=1
    end
  else
    s +=","+ map.to_s
  end
  return s
end

puts print_expression(cond_map, 0)
